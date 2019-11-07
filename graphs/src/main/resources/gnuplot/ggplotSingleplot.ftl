#!/usr/bin/R

library(tidyverse);
library(latex2exp);
library(ggplot2);
library(standardPrintOutput);

theme_set(defaultFigureLayout(base_size=10))

${plot}

saveSixthPageFigure(filename="${output}");
