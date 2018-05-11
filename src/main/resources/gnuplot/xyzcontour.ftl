#!/usr/bin/gnuplot -p
$data << EOD
<#list data as line>
${line}
</#list>
EOD
set title "${config.getTitle()}";
set xlabel "${config.getXLabel()}"; 
set ylabel "${config.getYLabel()}";
<#if config.getXScale()??>set xr ${config.getXScale()};</#if>
<#if config.getYScale()??>set yr ${config.getYScale()};</#if>
unset key;
set hidden3d
set term pngcairo enhanced font 'Verdana,10';
set output "${config.getOutputFile()}";
<#list config.getCustomCommands() as command>
${command};
</#list>
splot \
"$data" using ${config.indexOf("X")}:${config.indexOf("Y")}:${config.indexOf("Z")} with lines ;

<#-- 

set title "contours on both base and surface"
set contour both
set hidden3d
splot x**2-y**2 with lines, x**2-y**2 with labels boxed notitle


     set   autoscale                        # scale axes automatically
      unset log                              # remove any log-scaling
      unset label                            # remove any previous labels
      set xtic auto                          # set xtics automatically
      set ytic auto                          # set ytics automatically
      set title "Force Deflection Data for a Beam and a Column"
      set xlabel "Deflection (meters)"
      set ylabel "Force (kN)"
      set key 0.01,100
      set label "Yield Point" at 0.003,260
      set arrow from 0.0028,250 to 0.003,280
      set xr [0.0:0.022]
      set yr [0:325]
      plot    "force.dat" using 1:2 title 'Column' with linespoints , \
            "force.dat" using 1:3 title 'Beam' with points

     Create a title:                  > set title "Force-Deflection Data" 
      Put a label on the x-axis:       > set xlabel "Deflection (meters)"
      Put a label on the y-axis:       > set ylabel "Force (kN)"
      Change the x-axis range:         > set xrange [0.001:0.005]
      Change the y-axis range:         > set yrange [20:500]
      Have Gnuplot determine ranges:   > set autoscale
      Move the key:                    > set key 0.01,100
      Delete the key:                  > unset key
      Put a label on the plot:         > set label "yield point" at 0.003, 260 
      Remove all labels:               > unset label
      Plot using log-axes:             > set logscale
      Plot using log-axes on y-axis:   > unset logscale; set logscale y 
      Change the tic-marks:            > set xtics (0.002,0.004,0.006,0.008)
      Return to the default tics:      > unset xtics; set xtics auto

 
       set multiplot;                          # get into multiplot mode
      set size 1,0.5;  
      set origin 0.0,0.5;   plot sin(x); 
      set origin 0.0,0.0;   plot cos(x)
      unset multiplot                         # exit multiplot mode

plot "plotexp.dat" index 0:0 using 1:2:3 with yerrorbars,\
> "plotexp.dat" index 1:1 using 1:2:3 with yerrorbars,\
> "plotexp.dat" index 2:2 using 1:2:3 with yerrorbars

gnuplot> set style line 1 lt 1 lw 3
gnuplot> set style line 2 lt 1 pt 7
gnuplot> set style line 3 lt 1 pt 8
gnuplot> set style line 4 lt 1 pt 9
The first line defines the linestyle No.1 as the solid line with width of 3. The second to fourth lines define the linestyles those are used for experimental data. The line kind is solid, but the symbols of No.7, 8, and 9 are used.

set logscale x

http://lowrank.net/gnuplot/intro/plotfunc-e.html


You can continue your work interactively at the gnuplot command-line once the plot-file is loaded. However, a batch-mode is more convenient. Firstly, insert pause -1 at the end of the file, then
Edit the plot-file with a text editor
Browse the graph on a screen

wrap in shell script for substitition
#!/bin/sh
gnuplot << EOF
set terminal postscript eps color enhanced
set output "$1.eps"
set xlabel "Energy [MeV]"
set ylabel "Cross Section [b]"
set title "(n,2n) reaction"
set xrange [ 0 : 20 ]
set yrange [ 0 : 2 ]
set mxtics 5
set mytics 5
set xtics 5
set ytics 0.5
plot "$1.dat" using 1:2 notitle w l
EOF

## Different plot styles
http://lowrank.net/gnuplot/intro/style-e.html
http://gnuplot.info/docs_5.0/gnuplot.pdf page 54

## TIME:

set xdata time
set timefmt "%m/%d/%y"
set xrange ["03/21/95":"03/22/95"]
set format x "%m/%d"
set timefmt "%m/%d/%y %H:%M"
plot "data" using 1:3

-->