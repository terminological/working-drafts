#!/usr/bin/R


library(cowplot);
library(tidyverse);
library(latex2exp);
library(phdUtils);

theme_set(themePhd(base_size=10))

${plot}

saveThesisSixthPage("${output}");
