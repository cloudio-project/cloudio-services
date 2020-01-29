all: ca.cer server.cer cloudio_services.cer

ca.cer:
	@mkdir -p .signed
	@echo 01 > .serial
	@touch .index.txt
	@openssl req -x509 -config openssl.cnf -newkey rsa:2048 -days 3650 -out .ca.req -outform PEM -subj /CN=cloudio-dev-ca/ -nodes
	@openssl x509 -in .ca.req -out ca.cer -outform PEM

ca.jks:
	@keytool -J-Duser.language=en -import -alias authority -file ca.cer -keystore ca.jks

server.cer: ca.cer
	@openssl genrsa -out server.key 2048
	@openssl req -new -key server.key -out .server.req -outform PEM -subj /CN=\rabbitmq/O=broker/ -nodes
	@openssl ca -config openssl.cnf -in .server.req -out server.cer -notext -batch -extensions server_ca_extensions

cloudio_services.cer: ca.cer
	@openssl genrsa -out cloudio_services.key 2048
	@openssl req -new -key cloudio_services.key -out .cloudio_services.req -outform PEM -subj /CN=cloudio_services/O=client/ -nodes
	@openssl ca -config openssl.cnf -in .cloudio_services.req -out cloudio_services.cer -notext -batch -extensions client_ca_extensions
	@openssl pkcs12 -export -out cloudio_services.p12 -in cloudio_services.cer -inkey cloudio_services.key -password pass:

clean:
	@rm -rf .signed .[^.]* ca.* cloudio_services.* server.*
