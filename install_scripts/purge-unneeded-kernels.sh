Procedure to empty 100% /boot which cannot be fixed by apt-get autoremove nor apt-get -f install

0) become root

sudo -i

1) Check current kernel

#uname -r

3.16.0-56-generic

2) check boot
#cd /boot
#ls

pick, say 2 kernels with lowest numbers (different then current kernel) 
and manually remove associated files

#rm *-3.16.0-30*
#rm *-3.16.0-50*

3) there should be now enough space to perform fixes using apt

#df
(if not enough space, delete more kernels with rm as above)

#apt-get -f install
#apt-get autoremove 

