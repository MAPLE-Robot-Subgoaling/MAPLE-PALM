
/************************************************

	SOCKET CLASS
		Neville Mehta

*************************************************/


#pragma once

#if defined(_WIN32)
#include <winsock2.h>
#else
#define SOCKET int
#endif
#include <exception>
#include <string>


class Socket
{	private:
		unsigned* _num_references;
		static unsigned _num_of_sockets;

		static void initialize();

	protected:
		SOCKET _socket;

	public:
		Socket();
		Socket(const SOCKET&);
		Socket(const Socket&);
		Socket& operator = (const Socket& s);
		void send(std::string) const;
		bool is_readable(const long int& secs = 0, const long int& microsecs = 100000) const;  // Non-blocking call to check socket
		bool receive(std::string&) const;   // Blocking reception
		bool receive_timeout(std::string&, const unsigned& secs = 0, const unsigned& microsecs = 100000) const;   // Non-blocking reception (with timeout)
		virtual ~Socket();
};

//***********************************************************************************************//

class ClientSocket : public Socket
{	public:
		ClientSocket(const std::string&, const int&);
		virtual ~ClientSocket () {}
};

//***********************************************************************************************//

class ServerSocket : public Socket
{	public:
		ServerSocket(const int&, const int&, const bool non_blocking = false);
		Socket* accept() const;
		virtual ~ServerSocket () {}
};

//***********************************************************************************************//

class SocketException : public std::exception
{	private:
		std::string _error_message;

	public:
		SocketException (const std::string& error_message);
		const char* what () const throw() { return _error_message.c_str(); }
		~SocketException () throw() {}
};

//***********************************************************************************************//
