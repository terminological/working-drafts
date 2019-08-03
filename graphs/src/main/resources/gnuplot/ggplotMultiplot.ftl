#!/usr/bin/R

library(ggpubr);
# library(tikzDevice);
# library(cowplot);
library(ggplot2);
library(patchwork);
library(tidyverse);

theme_set(
	theme_bw(base_size=8,base_family="sans")+
	theme(
		plot.title=element_text(size=8,hjust=0.5),
		axis.title = element_text(size=8),
		axis.text.x = element_text(angle = 45, hjust = 1)
	)
)

<#list plots as plot>

${plot}

plot${plot?counter} <- plot<#if mergeAxes>+
theme(legend.position="none")<#if (plot?index) % cols != 0>+
theme(axis.title.y=element_blank(),axis.text.y=element_blank())</#if><#if (plot?index) < plots?size-cols>+
theme(axis.title.x=element_blank(),axis.text.x=element_blank())</#if></#if>;

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

# save_plot("${output}.png", p, ncol=${cols}, nrow=${(plots?size/cols)?int}, base_height=2, base_width=2);
ggsave("${output}.png", p, width = min(6,8*${cols}/${(plots?size/cols)?int}), height = min(8,6*${(plots?size/cols)?int}/${cols}));
ggsave("${output}.pdf", p, width = min(6,8*${cols}/${(plots?size/cols)?int}), height = min(8,6*${(plots?size/cols)?int}/${cols}));
# tikz(file = "${output}.tex", width = min(6,8*${cols}/${(plots?size/cols)?int}), height = min(8,6*${(plots?size/cols)?int}/${cols}))
# print(p)
# dev.off()

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
