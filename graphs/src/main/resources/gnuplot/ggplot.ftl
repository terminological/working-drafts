#!/usr/bin/R

library(tidyverse);
library(cowplot);

${data};

plot <- ggplot(df)+
<#list plots as plot>
${plot}<#sep>+
</#list>
labs(title="${config.getTitle()}", x="${config.getXLabel()}", y="${config.getYLabel()}");

save_plot("${output}", plot)