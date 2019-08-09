#!/usr/bin/R


library(cowplot);
library(tidyverse);
library(latex2exp);
library(phdUtils);

theme_set(themePhd(base_size=10))

${plot}

saveThesisSixthPage("${output}.png");
# ggsave("${output}.pdf", plot, width = 3*1.618, height = 3);
# tikz(file = "${output}.tex", width = 3*1.618, height = 3)
# print(plot)
# dev.off()