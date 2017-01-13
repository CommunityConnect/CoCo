#!/bin/bash
#  configures remote access via tigervnc
#assumes installation tigervncserver is done

#tigervncserver
#password setup needed

#tigervncserver -kill :1
mv ~/.vnc/xstartup ~/.vnc/xstartup.bak 
cd ~/.vnc

touch xstartup

echo "#!/bin/sh" >> xstartup
echo "xrdb $HOME/.Xresources" >> xstartup
echo "xsetroot -solid grey" >> xstartup
echo "export XKL_XMODMAP_DISABLE=1" >> xstartup
echo "/etc/X11/Xsession" >> xstartup
echo "/usr/bin/lxsession -s Lubuntu -e LXDE" >> xstartup

chmod +x xstartup

tigervncserver -geometry 1900x1000

cd
