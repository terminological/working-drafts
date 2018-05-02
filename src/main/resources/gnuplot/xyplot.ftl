
$data << EOD
<#list data as line>
${line}
</#list>
EOD
set title '${config.getTitle()}';
set xlabel '${config.getXLabel()}'; 
set title '${config.getYLabel()}';
plot "$data" using 1:2 with lines;
plot "$data" using 1:2 with points;