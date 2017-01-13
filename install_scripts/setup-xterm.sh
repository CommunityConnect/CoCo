#!/bin/bash

#xterm (frequently used by mininet) has by default has some unreadable font
#WARNING: if you already have something in ~/.Xresources this will (probably) break your system
#clean lubuntu desktop install comes without this file so no sweat ;)


echo "! xterm ----------------------------------------------------------------------" >> ~/.Xresources

echo "xterm*VT100.geometry:     80x25" >> ~/.Xresources
echo "xterm*faceName:           Terminus:style=Regular:size=11" >> ~/.Xresources
echo "!xterm*font:              -*-dina-medium-r-*-*-16-*-*-*-*-*-*-*" >> ~/.Xresources
echo "xterm*dynamicColors:      true" >> ~/.Xresources
echo "xterm*utf8:               2" >> ~/.Xresources
echo "xterm*eightBitInput:      true" >> ~/.Xresources
echo "xterm*saveLines:          512" >> ~/.Xresources
echo "xterm*scrollKey:          true" >> ~/.Xresources
echo "xterm*scrollTtyOutput:    false" >> ~/.Xresources
echo "xterm*scrollBar:          true" >> ~/.Xresources
echo "xterm*rightScrollBar:     true" >> ~/.Xresources
echo "xterm*jumpScroll:         true" >> ~/.Xresources
echo "xterm*multiScroll:        true" >> ~/.Xresources
echo "xterm*toolBar:            true" >> ~/.Xresources

