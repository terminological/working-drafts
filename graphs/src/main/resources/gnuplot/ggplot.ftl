#!/usr/bin/R

# library(tidyverse);
library(cowplot);

${data}

schemeName <- "${schemeName}";

plot <- ggplot(df)+
<#list plots as plot>
${plot}+
</#list>+
<#if config.getXmin()??>xlim(${config.getXmin()},${config.getXmax()})+</#if>
<#if config.getYmin()??>ylim(${config.getYmin()},${config.getYmax()})+</#if>
labs(title="${config.getTitle()}", x="${config.getXLabel()}", y="${config.getYLabel()}");

save_plot("${output}", plot);