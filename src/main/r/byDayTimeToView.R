# Daily time to view time series analysis

library(ggplot2)
library(dplyr)
library(reshape2)
library(ggrepel)
library(cowplot)
library(ggpubr)

source_directory <- getSrcDirectory(function(dummy) {dummy});
if (source_directory == "") source_directory="/home/robchallen/Git/working-drafts/src/main/r"
source(paste(source_directory,'/standardAxes.r',sep=""));
source(paste(source_directory,'/utils.r',sep=""));
createDirectory();

################
# DATA

data_by_day <- data_baseline %>%
  filter(time_of_week<=(120*60)) %>%
  group_by(time_of_day) %>%
  summarize(
    count=n(),
    unviewed_count=sum(total_views == 0),
    unviewed_percent=unviewed_count/count,
    median_ttv=fivenum(minutes_to_view, na.rm = TRUE)[3]
  )
data_by_day['roll_count'] <- stats::filter(data_by_day['count'],filter=rep(1/59,59),method='convolution',sides=2,circular=TRUE)
data_by_day['roll_unviewed_count'] <- stats::filter(data_by_day['unviewed_count'],filter=rep(1/59,59),method='convolution',sides=2,circular=TRUE)
data_by_day['roll_median_ttv'] <- stats::filter(data_by_day['median_ttv'],filter=rep(1/59,59),method='convolution',sides=2,circular=TRUE)
data_by_day <- mutate(data_by_day, roll_unviewed_percent = roll_unviewed_count/roll_count)

viewed_data_by_day <- viewed_data_baseline %>%
  filter(time_of_week_viewed<=(120*60)) %>%
  group_by(time_of_day_viewed) %>%
  summarize(
    count=n(),
    median_age=fivenum(minutes_to_view, na.rm = TRUE)[3]
  )
viewed_data_by_day['roll_count'] <- stats::filter(viewed_data_by_day['count'],filter=rep(1/59,59),method='convolution',sides=2,circular=TRUE)
viewed_data_by_day['roll_median_age'] <- stats::filter(viewed_data_by_day['median_age'],filter=rep(1/59,59),method='convolution',sides=2,circular=TRUE)

##################
# GRAPHS

by_day_plot_1 <- plot_day_xaxis(
  ggplot(data_by_day, aes(x=time_of_day/60)) +
  geom_point(aes(y = median_ttv),color='#808080',size=1) +
  geom_line(aes(y =roll_median_ttv),colour='black',size=1)+
  ylab("minutes")+
  scale_y_continuous(limits=c(0,180), breaks = seq(0, 240, 60))
)
by_day_plot_1+ggtitle("median time to view test result over day")
ggsave('medianTtvByDay.png',width=10,height=5,units='in')

by_day_plot_2 <- plot_day_xaxis(
  ggplot(data_by_day, aes(x=time_of_day/60)) +
  scale_y_continuous(limits=c(0,15),breaks = seq(0, 100, 5)) +
  geom_point(aes(y = unviewed_percent*100), colour='#ff8080', size = 1) +
  geom_line(aes(y = roll_unviewed_percent*100),colour='red',size=1)+
  ylab("percent")
)
by_day_plot_2+ggtitle("proportion of unviewed test results per minute over day")
ggsave('unviewedRatesByDay.png',width=10,height=5,units='in')

by_day_plot_3 <- plot_day_xaxis(ggplot() +
  geom_point(data = data_by_day, aes(x=time_of_day/60, y = count), colour='#d080d0', size = 1)+
  geom_point(data=viewed_data_by_day, aes(x=time_of_day_viewed/60, y = count),color='#8080ff',size=1)+
  geom_line(data = data_by_day, aes(x=time_of_day/60, y = roll_count),colour='magenta',size=1)+
  geom_line(data=viewed_data_by_day, aes(x=time_of_day_viewed/60, y = roll_count),colour='blue',size=1)+
  ylab("results/min")
)
by_day_plot_3+ggtitle("test results and result views over day")
ggsave('resultAndViewRatesByDay.png',width=10,height=5,units='in')

by_day <- plot_grid(by_day_plot_3+rremove("x.text")+rremove("xlab"),
                     by_day_plot_2+rremove("x.text")+rremove("xlab"),
                     by_day_plot_1,
                     nrow=3,
                     align="v",
                     labels = c("A","B","C"))
by_day;
save_plot("byDay.png", by_day,
          base_height = 10,
          base_width = 10
)

by_day2 <- plot_grid(by_day_plot_3+ggtitle("test results and result views")+rremove("x.text")+rremove("xlab"),
                    by_day_plot_2+ggtitle("probability of unviewed test result")+rremove("x.text")+rremove("xlab"),
                    by_day_plot_1+ggtitle("median time to view test result"),
                    nrow=3,
                    align="v"
                    )
by_day2;
save_plot("byDay2.png", by_day2,
          base_height = 10,
          base_width = 10
)

####################
# TODO:

ttvByCountTests <- ggplot(data_by_day)+
  geom_point(aes(x=count, y=median_ttv), colour="#b0b0b0",size=1)+
  # geom_path(aes(x=roll_count, y=roll_median_ttv),size=1)+
  geom_smooth(aes(x=count, y=median_ttv), method = "lm", se = FALSE,colour="black")+
  xlab("reports available/min")+
  ylab("minutes to view")
ttvByCountTests+ggtitle("Time to view result by numbers of reports published per minute")
ggsave('ttvByResultAvailability.png',width=10,height=5,units='in')

unviewedByCountTests <- ggplot(data_by_day)+
  geom_point(aes(x=count, y=unviewed_count), colour="#ffb0b0",size=1)+
  geom_smooth(aes(x=count, y=unviewed_count), method = "lm", se = FALSE,colour="red",size=1)+
  xlab("reports available/min")+
  ylab("unviewed reports/min")
unviewedByCountTests+ggtitle("Unviewed reports by numbers of reports published per minute")
ggsave('unviewedByResultAvailability.png',width=10,height=5,units='in')


syncToDrive();
