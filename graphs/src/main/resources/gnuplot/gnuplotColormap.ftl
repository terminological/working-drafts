
http://gnuplot.sourceforge.net/demo/pm3dcolors.html

#
# $Id: pm3dcolors.dem,v 1.3.4.1 2010/10/31 19:10:24 mikulik Exp $
#
# Test of color modes for pm3d palettes.

#
# Multiplot with some of the recommended rgbformulae palettes
#
set pm3d map
set multiplot layout 3,3 \
    title "Palettes according to 'help palette rgbformulae'"
g(x)=x
set xrange [0:1]; set xtics 0.2 scale 1.5 nomirror; set mxtics 2
# set palette maxcolors 128
unset colorbox; unset key; set tics out; unset ytics
set palette rgb 7,5,15; set title "traditional pm3d\n(black-blue-red-yellow)"; splot g(x)
set palette rgb 3,11,6; set title "green-red-violet"; splot g(x)
set palette rgb 23,28,3; set title "ocean (green-blue-white)\ntry also other permutations"; splot g(x)
set palette rgb 21,22,23; set title "hot (black-red-yellow-white)"; splot g(x)
set palette rgb 30,31,32; set title "color printable on gray\n(black-blue-violet-yellow-white)"; splot g(x)
set palette rgb 33,13,10; set title "rainbow (blue-green-yellow-red)"; splot g(x)
set palette rgb 34,35,36; set title "AFM hot (black-red-yellow-white)"; splot g(x)
set palette model HSV
set palette rgb 3,2,2; set title "HSV model\n(red-yellow-green-cyan-blue-magenta-red)"; splot g(x)
set pal gray; set title "gray palette"; splot g(x)
unset multiplot


#
#   Gradient Palettes
#
set pm3d map
set palette color

f(x)=(x+10)/20
set cbrange [f(-10):f(10)] # [0:1]
set xrange [-10:10]
set yrange [*:*]
set xtics 2
set cbtics 0.1
set format cb "%3.1f"
unset ztics
unset ytics
set samples 101
set isosamples 2
unset key

set palette model RGB

set palette defined 
set title "set palette defined"
splot f(x)


# https://github.com/Gnuplotting/gnuplot-palettes