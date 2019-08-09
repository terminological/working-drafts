
${data}

schemeName = "${schemeName}";

plot <- ggplot(df)+
lims(<#list config.getGGplotLimits() as scale>${scale}<#sep>, </#sep></#list>)+
<#list plots as plot>
${plot}+
</#list>
labs(<#list config.getGGplotLabels() as label>${label}<#sep>, </#sep></#list>)<#if includePlotTitles>+
ggtitle(TeX("${config.getTitle()}"))</#if>;
