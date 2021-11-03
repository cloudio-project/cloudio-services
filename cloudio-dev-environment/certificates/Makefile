all: ca.cer server.cer

ca.cer:
	@mkdir -p .signed
	@echo 01 > .serial
	@touch .index.txt
	openssl req -x509 -config openssl.cnf -newkey rsa:2048 -days 3650 -out .ca.req -outform PEM -subj /CN=cloudio-dev-ca/ -nodes
	openssl x509 -in .ca.req -out ca.cer -outform PEM

server.key:
	openssl genrsa -out server.key 2048

server.cer: server.key ca.cer
	openssl req -new -key server.key -out .server.req -outform PEM -subj /CN=\rabbitmq/O=broker/ -nodes
	openssl ca -config openssl.cnf -in .server.req -out server.cer -notext -batch -extensions server_ca_extensions

clean:
	@rm -rf .signed .[^.]* ca.* cloudio_services.* server.*
