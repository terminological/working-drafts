#!/usr/bin/R


library(tikzDevice);
library(cowplot);
theme_set(theme_bw())

${plot}

save_plot("${output}", plot);
tikz(file = "${output}.tex", width = pageWidth, height = pageWidth*323/716)
print(plot)
dev.off()