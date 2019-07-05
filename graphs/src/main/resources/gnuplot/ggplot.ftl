#!/usr/bin/R

# library(tidyverse);
library(cowplot);

${data}

schemeName <- "${schemeName}";

plot <- ggplot(df)+
<#list plots as plot>
${plot}+
</#list>
lims(<#list config.getGGplotScales() as scale>${scale}<#sep>, </#sep></#list>)+
labs(title="${config.getTitle()}"<#list config.getGGplotLabels() as label>, ${label}</#list>);

save_plot("${output}", plot);