#!/usr/bin/perl

################################################################################
# Runs a script to scan for an open port, runs Stratagus and tells it to open  #
# the socket interface on the open port, then runs your client and tells it to #
# connect to this port.                                                        #
################################################################################

########################## BEGIN CONFIGURABLE OPTIONS #########################

# Change to the directory containing the Stratagus executable
my $STRATAGUS_DIR = '../other/stratagus';

# Extra arguments to pass to Stratagus (the data directory, the port, and
# the map arguments are already passed so there's no need to include them here)
my $STRATAGUS_OPTS = '-l';

# Modify this if the data directory for Stratagus is in a non-standard location
my $STRATAGUS_DATA = "${STRATAGUS_DIR}/data";

# The location of the map relative to $STRATAGUS_DATA
my $STRATAGUS_MAP = '';

# Modify this if you want to change the default port where scanning starts
my $START_PORT = 4870;

# Maximum numbers of ports to try before giving up
my $MAX_PORTS = 100;

# Xvfb executable
chop(my $XVFB = `which Xvfb 2> /dev/null`);

# xauth executable
chop(my $XAUTH = `which xauth 2> /dev/null`);

# Client command-line where any instance of the string "_PORT_" will be
# replaced by the actual port before the command is run (providing this option
# on the command line will override whatever you have here)
my $CLIENT_CMD = '';

########################## END CONFIGURABLE OPTIONS ###########################

use Fcntl;
use Fcntl ':flock';
use File::Temp ':mktemp';
use Getopt::Std;
use IO::Socket;
use POSIX;


$0 =~ m|.*/(.*)|;
my $progname = $1;

sub usage {
	print STDERR << "EOF";

Usage: $progname [-g] [-m map-file] [client-command-line]

    -g                      By default, we run a virtual X server (requires
                            Xvfb to be installed) and connect Stratagus to it so
                            no graphics are displayed. Use this option if you
                            actually want to display the graphics.
    -m map-file             The path of the map relative to the data directory.
    client-command-line     Command line used to run your client. Any instance
                            of the string "_PORT_" will be replaced by the actual
                            port that Stratagus used to open the socket
                            interface. This option is required unless you
                            specify a default value at the top of the script.

    Example: If the command to run your client is "./client -p port" and you
             want to run Stratagus with the map map.pud.gz loaded, use the
             following command:

             $progname -m maps/map.pud.gz ./client -p _PORT_

EOF
    exit;
}

my $locked = 0;
my $xvfb_pid;
my $stratagus_pid;
my $show_graphics = 0;
my $xserver_num;
my $xauthority;
my $xauth_added = 0;
my $lock_file_handle;
my $lock_filename = "/tmp/transfer-lock";

sub get_lock {
	sysopen($lock_file_handle, "$lock_filename", O_RDONLY | O_CREAT) or die "Failed to open $lock_filename: $!";
	chmod(0666, $lock_filename);

	my $retries = 60;
	while (!flock($lock_file_handle, LOCK_EX | LOCK_NB)) {
		if ($retries-- == 0) {
			die "Failed to obtain file lock on $lock_filename: $!";
		}
		sleep(1);
	}

	$locked = 1;
}

sub release_lock {
	close($lock_file_handle) or die "Failed to close $lock_filename: $!";
	$locked = 0;
}

# Clean up
END {
	system("kill -9 $stratagus_pid 2> /dev/null") if $stratagus_pid > 0;
	system("kill $xvfb_pid 2> /dev/null") if $xvfb_pid > 0;
	system("$XAUTH -f $xauthority remove :$xserver_num") if $xauth_added;
	unlink($xauthority) if $xauth_added;
	release_lock() if $locked;
	if ($xvfb_pid || $stratagus_pid) {
		print "\nClean-up successful.\n";
	}
}

# Make sure our cleanup function is called when we exit
use sigtrap qw(handler END normal-signals error-signals);

# Parse command-line options
my %options = ();
getopts("hgm:", \%options);

# Print usage if the -h option is given or we don't have the info we need
usage() if defined $options{h};
usage() if !$ARGV[0] and !$CLIENT_CMD;

$show_graphics = 1 if defined $options{g};
if (!$show_graphics) {
	if (not -e "$XAUTH") {
		die "Can't find xauth executable; try running with -g option";
	}
	elsif (not -e "$XVFB") {
		die "Can't find Xvfb executable; try running with -g option";
	}
}

# Use Stratagus map from command line if specified
$STRATAGUS_MAP = $options{m} if defined $options{m};
if (not -e "${STRATAGUS_DATA}/${STRATAGUS_MAP}") {
	die "Can't find Stratagus map: ${STRATAGUS_DATA}/${STRATAGUS_MAP}";
}

# Check for the Stratagus executable
if (not -e "${STRATAGUS_DIR}/stratagus") {
	die "Can't find Stratagus executable: ${STRATAGUS_DIR}/stratagus";
}

# Check for the client executable
if (not -e "$ARGV[0]") {
	die "Can't find client executable: $ARGV[0]";
}

# Checking client command for "_PORT_" string
if (!$CLIENT_CMD) {
	$CLIENT_CMD = join(" ", @ARGV);
}
die "Client command must contain _PORT_" unless $CLIENT_CMD =~ /_PORT_/;


# Clean up dead children without wait()ing
$SIG{'CHLD'} = 'IGNORE';

# Only allow one instance of the script to scan for ports at a time
get_lock();

# Scan ports until we find an open one
my $socket;
$MAX_PORTS = $START_PORT + $MAX_PORTS;
for ( ; $START_PORT < $MAX_PORTS; $START_PORT++) {
	$socket = new IO::Socket::INET(LocalHost => 'localhost', LocalPort => $START_PORT, Proto => 'tcp', ReuseAddr => 1);

	# If socket connection succeeded (port is free), break out of the loop
	last if $socket;
}
die "Failed to find an open port" if $START_PORT == $MAX_PORTS;
close($socket);

# Block SIGINT while forking
$sigset = POSIX::SigSet->new(SIGINT);
sigprocmask(SIG_BLOCK, $sigset) or die "Can't block SIGINT for fork: $!";

# Run Xvfb if needed
if (!$show_graphics) {
	# Find the first free X server number
	$xserver_num = 100;
	while (-f "/tmp/.X${xserver_num}-lock" || -e "/tmp/.X11-unix/X$xserver_num") {
		$xserver_num++;
	}

	$xauthority = mktemp("/tmp/transfer.XXXXXX");
	$ENV{'XAUTHORITY'} = $xauthority;

	$xvfb_pid = fork();
	if ($xvfb_pid == 0) {
		# This is the child -- run Xvfb
		open(STDOUT, '>/dev/null');
		open(STDERR, '>/dev/null');
		system("$XAUTH -f $xauthority add :$xserver_num . `mcookie`");
		exec "${XVFB} :$xserver_num";
	}
	else {
		print "Running Xvfb (with Xauthority)\n";
	}
	die "Fork failed: $!" unless $xvfb_pid;
	$xauth_added = 1;
}

# Run Stratagus
$stratagus_pid = fork();
if ($stratagus_pid == 0) {
	# This is the child -- run Stratagus
	open(STDOUT, '>/dev/null');
	open(STDERR, '>/dev/null');
	$ENV{'DISPLAY'} = "localhost:${xserver_num}.0" if !$show_graphics;
	exec "${STRATAGUS_DIR}/stratagus -d $STRATAGUS_DATA -p $START_PORT $STRATAGUS_OPTS $STRATAGUS_MAP";
}
else {
	print "Running Stratagus: ${STRATAGUS_DIR}/stratagus -d $STRATAGUS_DATA -p $START_PORT $STRATAGUS_OPTS $STRATAGUS_MAP\n";
}
die "Fork failed: $!" unless $stratagus_pid;

# Unblock SIGINT
sigprocmask(SIG_UNBLOCK, $sigset) or die "Can't unblock SIGINT for fork: $!";

# Wait until Stratagus has opened the port and is accepting connections
print "Waiting for the Stratagus server...\n";
$socket = 0;
$retries = 60;
while (!$socket && $retries-- > 0) {
	$socket = new IO::Socket::INET(PeerAddr => "localhost:$START_PORT", Proto => 'tcp', Timeout => '1');
	sleep(1);
}
die "Tired of waiting for Stratagus" unless $socket;
close($socket);

print "Stratagus server ready.\n";
release_lock();

# Replacing _PORT_ with the value of $START_PORT
$CLIENT_CMD =~ s/_PORT_/$START_PORT/;

# Run the client
print "Running client: $CLIENT_CMD\n";
system($CLIENT_CMD);

exit;
