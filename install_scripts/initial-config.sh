#!/bin/bash
#initial config

# do we need it? otherwise no sshd_config
sudo apt-get install openssh-server -y

#keys
ssh-keygen -f /home/${USER}/.ssh/id_rsa -N ""
cp ~/.ssh/id_rsa.pub ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys

echo "" | sudo tee -a /etc/sudoers
echo "$USER  ALL=NOPASSWD: ALL" | sudo tee -a /etc/sudoers

#change prompt character
cp ~/.bashrc ~/.bashrc-backup

sed -i 's/\[\\033\[00m\\\]\\\$/\[\\033\[00m\\\]\\\>/g' ~/.bashrc
sed -i 's/\\u@\\h:\\w\\\$/\\u@\\h:\\w\\\>/g' ~/.bashrc

echo "" | sudo tee -a /etc/ssh/sshd_config
echo "UseDNS no" | sudo tee -a /etc/ssh/sshd_config



# WARNING
# We need to install key on another machine using ssh-copy-id 10.0.1.1
