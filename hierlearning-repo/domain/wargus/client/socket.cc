
/************************************************

	SOCKET CLASS
		Neville Mehta

*************************************************/


#if !defined(_WIN32)
#include <arpa/inet.h>
#include <cerrno>
#include <netdb.h>
#include <netinet/tcp.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

#define INVALID_SOCKET -1
#define SOCKET_ERROR -1
#endif
#include <cstring>
#include <iostream>
#include <sstream>
#include "socket.h"


const int buffer_size = 4096;
unsigned Socket::_num_of_sockets = 0;


Socket::Socket ()
{	initialize();

	_socket = socket(AF_INET, SOCK_STREAM, 0);   // TCP socket
	if (_socket == INVALID_SOCKET)
		throw SocketException("Socket::Socket");

	++_num_of_sockets;
	_num_references = new unsigned(1);
}


Socket::Socket (const SOCKET& s) : _socket(s)
{	initialize();
	_num_references = new unsigned(1);
}


Socket::Socket (const Socket& s) : _socket(s._socket)
{	_num_references = s._num_references;
	(*_num_references)++;
}


Socket& Socket::operator = (const Socket& s)
{	_socket = s._socket;
	_num_references = s._num_references;
	(*_num_references)++;

	return *this;
}


#if defined(_WIN32)
void Socket::initialize ()
{	if (!_num_of_sockets)
	{	WSADATA wsa_data;
		if (WSAStartup(MAKEWORD(2, 2), &wsa_data))
			throw SocketException("Socket::initialize");
	}
}
#else
void Socket::initialize () {}
#endif


void Socket::send (std::string message) const
{	do
	{	int sent_bytes = ::send(_socket, message.c_str(), int(message.size()), 0);
		if (sent_bytes == SOCKET_ERROR)
			throw SocketException("Socket::send");
		message.erase(0, sent_bytes);
	}
	while (message.size());
}


bool Socket::is_readable (const long int& secs, const long int& microsecs) const
{	fd_set fds;
	FD_ZERO(&fds);
	FD_SET(_socket, &fds);
	timeval tval = {secs, microsecs};   // Timeout in seconds & microseconds
	if (select(FD_SETSIZE, &fds, nullptr, nullptr, &tval) == SOCKET_ERROR)
		throw SocketException("Socket::is_readable");
	return FD_ISSET(_socket, &fds) ? true : false;
}


bool Socket::receive (std::string& message) const
{	char buffer[buffer_size];
	int recvd_bytes;
	unsigned long arg = 0;

	message.clear();
	do
	{	recvd_bytes = ::recv(_socket, buffer, buffer_size, 0);
		if (recvd_bytes == SOCKET_ERROR)
			throw SocketException("Socket::receive");
		message.append(buffer, recvd_bytes);
#if defined(_WIN32)
	} while (!ioctlsocket(_socket, FIONREAD, &arg) && arg > 0);
#else
	} while (!ioctl(_socket, FIONREAD, &arg) && arg > 0);
#endif

	return recvd_bytes ? true : false;   // Socket open?
}


bool Socket::receive_timeout (std::string& message, const unsigned& secs, const unsigned& microsecs) const
{	char buffer[buffer_size];
	int recvd_bytes = -1;

	message.clear();
	while (is_readable(secs, microsecs))
	{	recvd_bytes = ::recv(_socket, buffer, buffer_size, 0);
		if (recvd_bytes == 0)
			break;
		if (recvd_bytes == SOCKET_ERROR)
			throw SocketException("Socket::receive_delay");
		message.append(buffer, recvd_bytes);
	}

	return recvd_bytes ? true : false;   // Socket open?
}


Socket::~Socket ()
{	if (!--(*_num_references))
	{	delete _num_references;
		--_num_of_sockets;
#if defined(_WIN32)
		closesocket(_socket);
		if (!_num_of_sockets)
			WSACleanup();
#else
		close(_socket);
#endif
	}
}

//***********************************************************************************************//

ClientSocket::ClientSocket (const std::string& server_address, const int& port) : Socket()
{	std::cout << "Connecting to " << server_address << ":" << port << " ... ";
	sockaddr_in service;
	memset(&service, 0, sizeof(sockaddr_in));
	service.sin_family = AF_INET;
	service.sin_port = htons(port);

	if ((service.sin_addr.s_addr = inet_addr(server_address.c_str())) == INADDR_NONE)   // server_address in dotted quad notation?
	{	hostent* host_info;
		if ((host_info = gethostbyname(server_address.c_str())) == 0)   // server_address in DNS text notation?
			throw SocketException("ClientSocket::ClientSocket");
		service.sin_addr = *((in_addr*)host_info->h_addr);
	}

	if (connect(_socket, (sockaddr*)&service, sizeof(sockaddr)))
		throw SocketException("ClientSocket::ClientSocket");

	int flag = 1;
	if (setsockopt(_socket, IPPROTO_TCP, TCP_NODELAY, (char*)&flag, sizeof(flag)) == -1)
		throw SocketException("ClientSocket::ClientSocket");
	std::cout << "done.\n";
}

//***********************************************************************************************//

ServerSocket::ServerSocket (const int& port, const int& backlog, const bool non_blocking) : Socket()
{	sockaddr_in service;
	memset(&service, 0, sizeof(sockaddr_in));
	service.sin_family = AF_INET;
	service.sin_addr.s_addr = INADDR_ANY;
	service.sin_port = htons(port);

	int opt = 1;
	setsockopt(_socket, SOL_SOCKET, SO_REUSEADDR, (char*)&opt, sizeof(opt));

	if (non_blocking)
	{	unsigned long arg = 1;
#if defined(_WIN32)
		ioctlsocket(_socket, FIONBIO, &arg);
#else
		ioctl(_socket, FIONBIO, &arg);
		
//		int opts = fcntl(_socket, F_GETFL);
//		fcntl(_socket, F_SETFL, opts | O_NONBLOCK);
#endif
	}

	if (bind(_socket, (sockaddr*)&service, sizeof(sockaddr)) == SOCKET_ERROR)
		throw SocketException("ServerSocket::ServerSocket");

	if (listen(_socket, backlog) == SOCKET_ERROR)
		throw SocketException("ServerSocket::ServerSocket");
}


Socket* ServerSocket::accept () const
{	SOCKET new_socket = ::accept(_socket, nullptr, nullptr);

	if (new_socket == INVALID_SOCKET)
	{
#if defined(_WIN32)
		if (WSAGetLastError() != WSAEWOULDBLOCK)
#else
		if (errno != EWOULDBLOCK)
#endif
			throw SocketException("ServerSocket::accept");
	}

	return new Socket(new_socket);
}

//***********************************************************************************************//

SocketException::SocketException (const std::string& error_message)
{	std::ostringstream error_message_stream;
	error_message_stream << "\nException in " << error_message << ".";
#if defined(_WIN32)
	error_message_stream << "  Socket error: " << WSAGetLastError();
#else
	error_message_stream << "  Socket error: " << errno;
#endif
	_error_message = error_message_stream.str();
}
