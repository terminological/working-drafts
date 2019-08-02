#!/usr/bin/R


library(tikzDevice);
library(cowplot);
library(tidyverse);
theme_set(theme_bw())

${plot}

save_plot("${output}", plot);
tikz(file = "${output}.tex", width = 3*1.618, height = 3)
print(plot)
dev.off()