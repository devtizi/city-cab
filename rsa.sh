#LINUX SHELL SCRIPT TO GENERATE RSA KEY PAIR AND SET PERMISSIONS

sudo openssl genrsa -out private_key.pem 4096
sudo openssl rsa -in private_key.pem -pubout -out public_key.pem

sudo mkdir -p /etc/citycab/keys
sudo cp private_key.pem /etc/citycab/keys/
sudo cp public_key.pem /etc/citycab/keys/
sudo chown $USER /etc/citycab/keys/private_key.pem
sudo chmod 600 /etc/citycab/keys/private_key.pem
sudo chmod 644 /etc/citycab/keys/public_key.pem
