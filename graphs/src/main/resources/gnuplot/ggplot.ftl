#!/usr/bin/R

# library(tidyverse);
library(cowplot);

${data}

schemeName <- "${schemeName}";

plot <- ggplot(df)+
<#list plots as plot>
${plot}+
</#list>+
<#if config.getXmin()??>set xr ${config.getXScale()};</#if>
<#if config.getYScale()??>set yr ${config.getYScale()};</#if>
labs(title="${config.getTitle()}", x="${config.getXLabel()}", y="${config.getYLabel()}");

save_plot("${output}", plot);