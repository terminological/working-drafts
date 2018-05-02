# ${test}
set title '${config.getTitle()}';
set xlabel '${config.getXLabel()}'; 
set title '${config.getYLabel()}';
plot - using 1:2 with lines;
plot - using 1:2 with points;