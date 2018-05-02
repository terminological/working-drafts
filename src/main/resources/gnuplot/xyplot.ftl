set title '${config.getTitle()}';
set xlabel '${config.xLabel}'; 
set title '${config.yLabel}';
plot - using 1:2 with lines;
plot - using 1:2 with points;