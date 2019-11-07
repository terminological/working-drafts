#!/usr/bin/R


library(cowplot);
library(tidyverse);
library(latex2exp);
library(standardPrintOutput);

theme_set(defaultFigureLayout(base_size=10))

${plot}

saveThesisSixthPage("${output}");
