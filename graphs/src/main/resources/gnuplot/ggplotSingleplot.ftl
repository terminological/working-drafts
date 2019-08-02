#!/usr/bin/R


# library(tikzDevice);
library(cowplot);
library(tidyverse);
theme_set(theme_bw(base_size=12,base_family="sans"))

${plot}

save_plot("${output}.png", plot);
ggsave("${output}.pdf", plot, width = 3*1.618, height = 3);
# tikz(file = "${output}.tex", width = 3*1.618, height = 3)
# print(plot)
# dev.off()