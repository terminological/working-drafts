#!/usr/bin/R

library(ggpubr);
library(ggplot2);
library(patchwork);
library(tidyverse);
library(latex2exp);

library(standardPrintOutput);

theme_set(defaultFigureLayout(base_size=8))

<#list plots as plot>

${plot}

plot${plot?counter} <- plot+<#if mergeAxes>
theme(legend.position="none")<#if (plot?index) % cols != 0>+
theme(axis.title.y=element_blank(),axis.text.y=element_blank())</#if><#if (plot?index) < plots?size-cols>+
theme(axis.title.x=element_blank(),axis.text.x=element_blank())</#if><#else>
theme(legend.position = "bottom", legend.title=element_blank())</#if>;

</#list>

<#if mergeAxes>
grobs <- ggplotGrob(plot)$grobs
legend <- grobs[[which(sapply(grobs, function(x) x$name) == "guide-box")]]
</#if>

<#-- 
grid <- plot_grid(  
<#list plots as plot>
	plot${plot?counter},
</#list>
    ncol=${cols},
    align='hv')
p <- plot_grid(grid, legend, ncol = 2, rel_widths = c(1, .2))
-->

grid <- wrap_plots(<#list plots as plot>plot${plot?counter}<#sep>, </#sep></#list>,ncol=${cols});

<#if mergeAxes>
p <- wrap_plots(grid,wrap_elements(legend),nrow=1,widths = c(1, .2));
<#else>
p <- grid;
</#if>

saveFullPageFigure(plot=p,filename="${output}",aspectRatio=${cols}<#if mergeAxes>*1.2</#if>/${(plots?size/cols)?int})
<#-- https://stackoverflow.com/questions/45473843/put-row-and-column-titles-using-grid-arrange-in-r/45474093#45474093 
rremove

"grid" for both x and y grids
"x.grid" for x axis grids
"y.grid" for y axis grids
"axis" for both x and y axes
"x.axis" for x axis
"y.axis" for y axis
"xlab", or "x.title" for x axis label
"ylab", or "y.title" for y axis label
"xylab", "xy.title" or "axis.title" for both x and y axis labels
"x.text" for x axis texts (x axis tick labels)
"y.text" for y axis texts (y axis tick labels)
"xy.text" or "axis.text" for both x and y axis texts
"ticks" for both x and y ticks
"x.ticks" for x ticks
"y.ticks" for y ticks
"legend.title" for the legend title
"legend" for the legend

-->
