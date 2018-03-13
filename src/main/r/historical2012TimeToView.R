# By month
# Weekly time to view time series analysis
# install.packages("pwr")
# install.packages("stargazer")
# install.packages("htmlTable")
# install.packages("broom")
# install.packages("formattable")

library(ggplot2)
library(plyr)
library(dplyr)
library(reshape2)
library(ggrepel)
library(cowplot)
library(ggpubr)
library(scales)
library(pwr)
library(stargazer)
library(htmlTable)
library(formattable)
library(knitr)

source_directory <- getSrcDirectory(function(dummy) {dummy});
source(paste(source_directory,'/standardAxes.R',sep=""));
source(paste(source_directory,'/utils.R',sep=""));
createDirectory();

##############
# DATA

data_2012 <- data %>% filter(
  investigation_abnormal==1 
  & discipline_name=='Chem/Haem'
  & as.Date(date) < as.Date("2016-09-01")
  & as.Date(date) >= as.Date("2012-09-01")
);

timespans <- data.frame(
  xmin=c(as.Date("2013-02-01"),as.Date("2014-11-01")),
  xmax=c(as.Date("2014-02-01"),as.Date("2015-11-01")),
  label=c("pre","post")
)


h2012_data_by_day <- data_2012 %>% 
  mutate(time_group = as.Date(date)) %>%
  filter(time_group != as.Date("2015-09-06")) %>%
  group_by(time_group) %>%
  dplyr::summarize(
    count=n(),
    unviewed_count=sum(total_views == 0),
    unviewed_percent=sum(total_views == 0)/n(),
    lower_quartile_ttv=fivenum(minutes_to_view, na.rm = TRUE)[2],
    median_ttv=fivenum(minutes_to_view, na.rm = TRUE)[3],
    upper_quartile_ttv=fivenum(minutes_to_view, na.rm = TRUE)[4],
    views = sum(total_views),
    mean_views = mean(total_views)
  )

width <- 21 # days
norm_filter <- dnorm(seq(-2,2, length = width))/sum(dnorm(seq(-2,2, length = width)))

h2012_data_by_day['roll_count'] <- stats::filter(h2012_data_by_day['count'],filter=norm_filter,method='convolution',sides=2,circular=FALSE)
h2012_data_by_day['roll_unviewed_count'] <- stats::filter(h2012_data_by_day['unviewed_count'],filter=norm_filter,method='convolution',sides=2,circular=FALSE)
h2012_data_by_day['roll_median_ttv'] <- stats::filter(h2012_data_by_day['median_ttv'],filter=norm_filter,method='convolution',sides=2,circular=FALSE)
h2012_data_by_day['roll_lower_quartile_ttv'] <- stats::filter(h2012_data_by_day['lower_quartile_ttv'],filter=norm_filter,method='convolution',sides=2,circular=FALSE)
h2012_data_by_day['roll_upper_quartile_ttv'] <- stats::filter(h2012_data_by_day['upper_quartile_ttv'],filter=norm_filter,method='convolution',sides=2,circular=FALSE)
h2012_data_by_day['roll_views'] <- stats::filter(h2012_data_by_day['views'],filter=rep(norm_filter),method='convolution',sides=2,circular=FALSE)
h2012_data_by_day <- mutate(h2012_data_by_day, roll_unviewed_percent = roll_unviewed_count/roll_count);
h2012_data_by_day <- mutate(h2012_data_by_day, roll_mean_views = roll_views/roll_count);


viewed_h2012_data_by_day <- data_2012 %>% 
  filter(date!=as.POSIXct("2015-09-06")) %>%
  filter(viewed) %>%
  mutate (time_group = as.Date(date)) %>%
  filter(time_group != as.Date("2015-09-06")) %>%
  group_by(time_group) %>%
  dplyr::summarize(
    count=n(),
    median_age=fivenum(minutes_to_view, na.rm = TRUE)[3]
  )
viewed_h2012_data_by_day['roll_count'] <- stats::filter(viewed_h2012_data_by_day['count'],filter=rep(norm_filter),method='convolution',sides=2,circular=TRUE)
viewed_h2012_data_by_day['roll_median_age'] <- stats::filter(viewed_h2012_data_by_day['median_age'],filter=rep(norm_filter),method='convolution',sides=2,circular=TRUE)

###############
# PLOTS

# Median time to view test result over week
h2012_plot_ttv <- 
  ggplot(
    h2012_data_by_day, 
    aes(x=time_group)) +
    xlab("date")+
  geom_rect(data=timespans, aes(xmin=xmin, xmax=xmax, ymin=-Inf, ymax=+Inf),fill="#e0e0e080",size=1,inherit.aes = FALSE)+
  scale_x_date(date_breaks="3 months",date_labels="%b %Y")+
  theme(axis.text.x = element_text(angle=45,size=9,hjust=1,vjust=1)) +
    ylab("minutes")+
    scale_y_continuous(limits=c(0,600), breaks = seq(0, 600, 60))+
    geom_point(aes(y = median_ttv),color='#808080',size=1)+
    geom_line(aes(y = roll_median_ttv, group =1), color="black",size = 1)+
  geom_point(aes(y = lower_quartile_ttv),color='#b0b0ff',size=1)+
  geom_line(aes(y = roll_lower_quartile_ttv, group =1), color="#8080ff",size = 1)+
geom_point(aes(y = upper_quartile_ttv),color='#8080ff',size=1)+
  geom_line(aes(y = roll_upper_quartile_ttv, group =1), color="blue",size = 1)

h2012_plot_ttv+ggtitle("median time to view test result over time")
ggsave('medianTtvH2012.png',width=10,height=5,units='in')

# Percent unviewed view test result over week
h2012_plot_unviewed <- 
  ggplot(h2012_data_by_day, aes(x=time_group)) +
  geom_rect(data=timespans, aes(xmin=xmin, xmax=xmax, ymin=-Inf, ymax=+Inf),fill="#e0e0e080",size=1,inherit.aes = FALSE)+
  xlab("date")+
  scale_x_date(date_breaks="3 months",date_labels="%b %Y")+
  theme(axis.text.x = element_text(angle=45,size=9,hjust=1,vjust=1)) +
  ylab("percent")+
#    scale_y_continuous(limits=c(0,20),breaks = seq(0, 100, 5)) +
    geom_point(aes(y = unviewed_percent*100),color='#ff8080',size=1)+
    geom_line(aes(y = roll_unviewed_percent*100, group=1), color="red",size = 1)

h2012_plot_unviewed+ggtitle("proportion of unviewed test results per day over time")
ggsave('unreviewedRatesH2012.png',width=10,height=5,units='in')

# totals viewed result over week
h2012_plot_counts <- 
  ggplot() +
  geom_rect(data=timespans, aes(xmin=xmin, xmax=xmax, ymin=-Inf, ymax=+Inf),fill="#e0e0e080",size=1,inherit.aes = FALSE)+
  xlab("date")+
  scale_x_date(date_breaks="3 months",date_labels="%b %Y")+
  theme(axis.text.x = element_text(angle=45,size=9,hjust=1,vjust=1)) +
    ylab("results/day")+
    # scale_y_continuous(breaks = seq(0, 100, 5)) +
    geom_point(data=h2012_data_by_day, aes(x=time_group,y = count),color='#ffb0ff',size=1)+
    geom_point(data=viewed_h2012_data_by_day, aes(x=time_group,y = count),color='#c0c0ff',size=1)+
    geom_line(data=h2012_data_by_day, aes(x=time_group,y = roll_count,group=1), color="magenta",size = 1)+
    geom_line(data=viewed_h2012_data_by_day, aes(x=time_group,y = roll_count,group=2), color="blue",size = 1)

h2012_plot_counts+ggtitle("test results and result views over time")
ggsave('resultAndViewRatesH2012.png',width=10,height=5,units='in')

# Median time to view test result over week
h2012_plot_viewers <- 
  ggplot() +
  geom_rect(data=timespans, aes(xmin=xmin, xmax=xmax, ymin=-Inf, ymax=+Inf),fill="#e0e0e080",size=1,inherit.aes = FALSE)+
  xlab("date")+
  scale_x_date(date_breaks="3 months",date_labels="%b %Y")+
  theme(axis.text.x = element_text(angle=45,size=9,hjust=1,vjust=1)) +
  ylab("no. viewers")+
  # scale_y_continuous(breaks = seq(0, 100, 5)) +
  geom_point(data=h2012_data_by_day, aes(x=time_group,y = mean_views),color='#ffd0b0',size=1)+
  geom_line(data=h2012_data_by_day, aes(x=time_group,y = roll_mean_views), color="orange",size = 1)

h2012_plot_viewers+ggtitle("average clinicians viewing over time")
ggsave('viewsH2012.png',width=10,height=5,units='in')


# http://www.sthda.com/english/articles/24-ggpubr-publication-ready-plots/81-ggplot2-easy-way-to-mix-multiple-graphs-on-the-same-page/#required-r-package
# https://cran.r-project.org/web/packages/cowplot/vignettes/introduction.html

h2012_data_by_day_pre <- h2012_data_by_day %>% filter(time_group >= timespans$xmin[1] & time_group < timespans$xmax[1])
h2012_data_by_day_post <- h2012_data_by_day %>% filter(time_group >= timespans$xmin[2] & time_group < timespans$xmax[2])

h2012_variance_ttv_pre <- ggplot(h2012_data_by_day_pre,aes(x=median_ttv))+geom_histogram(bins=20,fill='#808080',colour='black')+xlab("median time to view")+expand_limits(x=c(0,250))
h2012_variance_ttv_post <- ggplot(h2012_data_by_day_post,aes(x=median_ttv))+geom_histogram(bins=20,fill='#808080',colour='black')+xlab("median time to view")+expand_limits(x=c(0,250))

# tmp<-sd(h2012_data_by_day$median_ttv)
# power.t.test(n=91,power = 0.8, delta = NULL, sd=tmp, alternative = "one.sided",type="two.sample")

h2012_variance_unviewed_pre <- ggplot(h2012_data_by_day_pre, aes(x=unviewed_percent*100))+geom_histogram(bins=20,fill='#ff8080',colour='black')+xlab("propotion tests unviewed")+expand_limits(x=c(0,20))
h2012_variance_unviewed_post <- ggplot(h2012_data_by_day_post, aes(x=unviewed_percent*100))+geom_histogram(bins=20,fill='#ff8080',colour='black')+xlab("propotion tests unviewed")+expand_limits(x=c(0,20))

# tmp <- sd(h2012_data_by_day$unviewed_percent)

h2012_variance_viewers_pre <- ggplot(h2012_data_by_day_pre, aes(x=mean_views))+geom_histogram(bins=20,fill='#ffd0b0',colour='black')+xlab("mean number clinicians viewing")+expand_limits(x=c(0,3.5))
h2012_variance_viewers_post <- ggplot(h2012_data_by_day_post, aes(x=mean_views))+geom_histogram(bins=20,fill='#ffd0b0',colour='black')+xlab("mean number clinicians viewing")+expand_limits(x=c(0,3.5))

# tmp <- sd(h2012_data_by_day$mean_views)

h2012 <- plot_grid(
    h2012_plot_ttv+rremove("x.text")+rremove("xlab"),
    h2012_variance_ttv_pre+rremove("ylab")+rremove("xlab"),
    h2012_variance_ttv_post+rremove("ylab")+rremove("xlab"),
    h2012_plot_unviewed+rremove("x.text")+rremove("xlab"),
    h2012_variance_unviewed_pre+rremove("ylab")+rremove("xlab"),
    h2012_variance_unviewed_post+rremove("ylab")+rremove("xlab"),
    h2012_plot_viewers,
    h2012_variance_viewers_pre+rremove("ylab")+rremove("xlab"),
    h2012_variance_viewers_post+rremove("ylab")+rremove("xlab"),
    nrow=3,
    rel_widths=c(2,1,1),
    align="hv",
    labels = c("A1","A2","A3","B1","B2","B3","C1","C2","C3"))
#h2012;
save_plot("h2012Pub.png", h2012,
          base_height = 7.5,
          base_width = 10
)

##############
# regressions

tmpOrigin <- median(h2012_data_by_day_pre$time_group)
pre <- h2012_data_by_day_pre %>% mutate(days =as.numeric(difftime(time_group,tmpOrigin,units="day")))
post <- h2012_data_by_day_post %>% mutate(days =as.numeric(difftime(time_group,tmpOrigin,units="day")))

stargazer(
  lm(median_ttv~days,pre),
  lm(upper_quartile_ttv~days,pre),
  lm(lower_quartile_ttv~days,pre),
  lm(mean_views~days,pre),
  lm(unviewed_percent~days,pre),
  type="html" ,out="2012table1.html")



# unlist might be useful here


# http://my.ilstu.edu/~wjschne/444/IndependentSamples.html#(9)
# https://www.rstudio.com/wp-content/uploads/2015/02/data-wrangling-cheatsheet.pdf

var.test(pre$median_ttv,post$median_ttv)

ttest_median_ttv <- t.test(x=pre$median_ttv, y=post$median_ttv, alternative="g" )
ttest_uq_ttv <- t.test(x=pre$upper_quartile_ttv, y=post$upper_quartile_ttv, alternative="g" )
ttest_lq_ttv <- t.test(x=pre$lower_quartile_ttv, y=post$lower_quartile_ttv, alternative="g" )
ttest_unviewed <- t.test(x=pre$unviewed_percent*100, y=post$unviewed_percent*100, alternative="g" )
ttest_mean_views <- t.test(x=pre$mean_views, y=post$mean_views )

# data_frame(names(ttest_lq_ttv),ttest_median_ttv,ttest_uq_ttv,ttest_lq_ttv,ttest_unviewed,ttest_mean_views)
# ldply(c(ttest_median_ttv))#,ttest_uq_ttv)) #,ttest_lq_ttv,ttest_unviewed,ttest_mean_views))

ttest_df <- rbind(
      tidy(ttest_median_ttv),
      tidy(ttest_uq_ttv),
      tidy(ttest_lq_ttv),
      tidy(ttest_unviewed),
      tidy(ttest_mean_views)
    )
ttest_df$name = c("Median time to view", "Upper quartile time to view",
         "Lower quartile time to view",
         "Unviewed tests", "Mean clinicians viewing")
# formattable cannot reorder columns
# formattable(ttest_df, list(
#   conf.low = FALSE,
#   conf.high = FALSE,
#   statistic = FALSE,
#   parameter = FALSE)
# )


ttest_out <- data.frame(
  name = c("Median time to view", "Upper quartile time to view",
                   "Lower quartile time to view",
                   "Unviewed tests", "Mean clinicians viewing"
                   ))
ttest_out$alternative <- ttest_df$alternative
ttest_out$effect.size <- ttest_df$estimate
ttest_out$p.value <- ttest_df$p.value
ttest_out$pre.intervention <- ttest_df$estimate1
ttest_out$post.intervention <- ttest_df$estimate2

# export to file
cat(
  ttest_out %>% kable("html", digits=2),
  file="2012table2.html")

