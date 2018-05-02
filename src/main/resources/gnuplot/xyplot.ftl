#!/usr/bin/gnuplot -p
$data << EOD
<#list data as line>
${line}
</#list>
EOD
set title '${config.getTitle()}';
set xlabel '${config.getXLabel()}'; 
set ylabel '${config.getYLabel()}';
unset key;
set term png;
set output "${config.getOutputFile()}";
plot \
<#if config.hasDimension("Y_FIT")>
"$data" using ${config.indexOf("X")}:${config.indexOf("Y_FIT")} with lines, \
"$data" using ${config.indexOf("X")}:${config.indexOf("Y")} with points;
<#else>
"$data" using ${config.indexOf("X")}:${config.indexOf("Y")} with lines;
</#if>