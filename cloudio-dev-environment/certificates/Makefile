all: ca.cer ca.p12 ca.jks server.cer cloudio_services.cer cloudio_services.p12

ca.cer:
	@mkdir -p .signed
	@echo 01 > .serial
	@touch .index.txt
	openssl req -x509 -config openssl.cnf -newkey rsa:2048 -days 3650 -out .ca.req -outform PEM -subj /CN=cloudio-dev-ca/ -nodes
	openssl x509 -in .ca.req -out ca.cer -outform PEM


ca.p12: ca.cer
	openssl pkcs12 -export -out ca.p12 -in ca.cer -nokeys -password pass:

ca.jks: ca.cer
	@echo "cloudioDEV" > .temp
	@echo "cloudioDEV" >> .temp
	@echo "yes" >> .temp
	cat .temp | keytool -J-Duser.language=en -import -alias authority -file ca.cer -keystore ca.jks
	@rm .temp

server.key:
	openssl genrsa -out server.key 2048

server.cer: server.key ca.cer
	openssl req -new -key server.key -out .server.req -outform PEM -subj /CN=\rabbitmq/O=broker/ -nodes
	openssl ca -config openssl.cnf -in .server.req -out server.cer -notext -batch -extensions server_ca_extensions

cloudio_services.key:
	openssl genrsa -out cloudio_services.key 2048

cloudio_services.cer: cloudio_services.key ca.cer
	openssl req -new -key cloudio_services.key -out .cloudio_services.req -outform PEM -subj /CN=cloudio_services/O=client/ -nodes
	openssl ca -config openssl.cnf -in .cloudio_services.req -out cloudio_services.cer -notext -batch -extensions client_ca_extensions

cloudio_services.p12: cloudio_services.cer cloudio_services.key
	openssl pkcs12 -export -out cloudio_services.p12 -in cloudio_services.cer -inkey cloudio_services.key -password pass:

clean:
	@rm -rf .signed .[^.]* ca.* cloudio_services.* server.*
