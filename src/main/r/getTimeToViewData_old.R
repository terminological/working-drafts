# install.packages("car")

library(DBI)
library(odbc)
library(datasets)
library(signal)
library(dplyr)
library(reshape2)
library(GGally)
library(cowplot)
library(ggbeeswarm)
library(scales)
library(ggrepel)


source_directory <- getSrcDirectory(function(dummy) {dummy});
if (source_directory == "") source_directory="/home/robchallen/Git/working-drafts/src/main/r"
source(paste(source_directory,'/standardAxes.R',sep=""));
source(paste(source_directory,'/utils.R',sep=""));
createDirectory();

pwd <- readline(prompt="Enter DB: ");
con <- dbConnect(odbc(),
                 Driver = "ODBC Driver 13 for SQL Server",
                 Server = "10.174.129.118",
                 Database = "RobsDatabase",
                 UID = "RobertSQL",
                 PWD = pwd,
                 Port = 1433);

dbListTables(con);

data <- dbReadTable(con, "aggTimeToView", as.is=c(FALSE,TRUE,FALSE,FALSE,FALSE,FALSE,FALSE,FALSE,TRUE,FALSE,FALSE,FALSE,FALSE,
                                                  FALSE,FALSE,FALSE,FALSE,FALSE,FALSE,FALSE));

data <- data %>%
	mutate(
			minutes_to_view = ifelse(minutes_to_view > 31*24*60, NA, minutes_to_view),
			view_type = ifelse(is.na(minutes_to_view), NA, view_type),
			total_views = ifelse(is.na(minutes_to_view), 0, as.integer(total_views)),
			viewed = !is.na(minutes_to_view)
)

## Determine a smoothed value for the publication rate.
# This is based on oversampling for every minute of our time series, which 
# will be gappy.
# We fill the gaps using the cumulative total and then using a rolling mean based on the 30
# minutes before and after to calculate a smoothed cumumlative total
# The gradient of this is estimated on a minute by minute basis using a savitsky golay filter (m=1).
pub_rate <- data.frame(date=seq.POSIXt(from=as.POSIXct("2014-08-31"),to=as.POSIXct("2017-09-01"),by="1 min"))
pub_rate <- pub_rate %>% left_join(data %>% group_by(date) %>% summarise(events=n())) 
pub_rate <- pub_rate %>% mutate(events = ifelse(is.na(events),0,events))
pub_rate <- pub_rate %>% mutate(running = cumsum(events))
width = 61
pub_rate$smoothed_running <- as.numeric(stats::filter(pub_rate$running,filter=rep(1/width,width),method='convolution',sides=2,circular=FALSE))
pub_rate <- pub_rate %>% filter(!is.na(smoothed_running))
pub_rate$publish_rate <- signal::sgolayfilt(pub_rate$smoothed_running,m=1)
pub_rate <- pub_rate %>% select(date,publish_rate)

# sg <- sgolay(p=5,n=width,m=0)
# 
# summary(data$interval)
# tmp <- signal::filter(sg,data$interval)
# summary(tmp)
# summary(data$interval)
# data$publish_rate <- 1/signal::filter(sg,data$interval)
# 
# summary(data$publish_rate)
# augment the data with the instantaneous publish rate.
data <- data %>% inner_join(pub_rate,by="date")

data <- data %>% filter(
  as.Date(date) <= as.Date("2017-08-31") 
  & ward_name != "Other"
  & as.Date(date) >= as.Date("2014-09-01")
);

data <- data %>% mutate(month_cat=format(date, "%Y-%m")) %>%
  mutate(time_of_week=as.numeric(difftime(date,cut(date,"week"),unit="mins"))%%(24*7*60)) %>%
  mutate(time_of_day=time_of_week %% (24*60)) %>%
  mutate(time_of_week_viewed=as.numeric(difftime(first_viewed_date,cut(first_viewed_date,"week"),unit="mins"))%%(24*7*60)) %>%
  mutate(time_of_day_viewed=time_of_week_viewed %% (24*60)) %>%
  mutate(end_month_cat=format(first_viewed_date, "%Y-%m")) %>%
  collect();

data <- data %>%  mutate(day_time_cat = cut(
  (time_of_day+16*60)%%(24*60),
  breaks=c(0,4*60,8*60,10*60,25*60),
  labels=c("08:00-11:59","12:00-15:59","16:00-17:59","18:00-07:59"),
  ordered_result=FALSE,
  include.lowest=TRUE
));

data_baseline <- data %>% filter(
  investigation_abnormal==1 
  & discipline_name=='Chem/Haem'
  & as.Date(date) < as.Date("2017-09-01")
  & as.Date(date) >= as.Date("2014-09-01")
  & dependency_level != 'PRIVATE'
);

save.image(file="~/R/timeToViewData");
load(file="~/R/timeToViewData");

#################################
# Helper functions

continuousNumericsToFactors <- function(v,h) {
  return(
    cut(v, breaks=c(-Inf,seq(1,h),Inf), labels=c(paste0(seq(0,h-1),"-",seq(1,h)),paste0(">",h)))
  )
}
discreteNumericsToFactors <- function(v,h) {
  return(
    cut(v, breaks=c(seq(0,h),Inf), labels=c(seq(0,h-1),paste0(h,"+")),right=FALSE)
  )
}
normaliseText <- function(v) {
  return(tolower(gsub("_"," ",v)))
}

##################################
# Deal with issues of category and abnormality
##################################

ggplot(data %>% group_by(label=discipline_name, fill=ifelse(investigation_abnormal==0,"normal","abnormal")) %>%
         filter(viewed & minutes_to_view > 0)
)+
  geom_violin(aes(x=label,y=minutes_to_view+1, fill=fill), position="dodge", colour="black")+
  # geom_boxplot(aes(x=label,y=minutes_to_view+1, fill=fill), position="dodge", width=0.4, colour="blue")+
  ylab("time to view")+
  xlab(NULL)+
  scale_fill_manual(values=c("normal"="#b0b0b0","abnormal"="#606060"), name=NULL)+
  scale_y_log10(breaks=c(2,60+1,(6*60)+1,(24*60)+1,(7*24*60)+1,(30*24*60)+1),labels=c("1m","1h","6h","1d","1w","30d"))+
  theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))

ggplot(data %>% group_by(label=discipline_name, fill=ifelse(investigation_abnormal==0,"normal","abnormal")) %>% 
         summarize(
           count=n(),
           unviewed_percent=sum(ifelse(viewed,0,1))/n()
         ) %>% mutate (
           std_error = sqrt(unviewed_percent*(1-unviewed_percent)/count)
         ) 
)+
  geom_bar(aes(x=label,y=100*unviewed_percent,fill=fill), stat="identity", position="dodge", colour="black")+
  geom_errorbar(aes(x=label,ymin=100*(unviewed_percent-1.96*std_error),ymax=100*(unviewed_percent+1.96*std_error),fill=fill), position="dodge", width=0.7)+
  scale_fill_manual(values=c("normal"="#ffb0b0","abnormal"="#ff6060"), name=NULL)+
  ylab("results unviewed (%)")+
  xlab(NULL)+
  expand_limits(y=c(0,30))+
  theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))

##################################
# Multivariate plots
##################################
# set of plots with y values of either % tests unviewed or time to view (logarithmic scale)
# x values will be our index variables
# binary (violin plot for ttv and a bar chart for tests unviewed):
#  investigation_abnormal
#  patient_gender
#  patient_id_updated
# categorical (same as binary):
#  emis (number identities)
#  investigation (top 20 only)
#  dependency_level
#  patient_group
#  month
#  day_of_week
# continuous (heat map with rolling IQR ribbon overlay for ttv; rolling )

###############
# BINARY / CATEGORICAL

unviewedPlot <- function(groupedByLabelled) {
  return(
    ggplot(groupedByLabelled %>% 
        summarize(
          count=n(),
          unviewed_percent=sum(ifelse(viewed,0,1))/n()
        ) %>% mutate (
          std_error = sqrt(unviewed_percent*(1-unviewed_percent)/count)
        ) 
    )+
    geom_bar(aes(x=label,y=100*unviewed_percent), stat="identity", position="dodge", colour="black", fill="#ff8080")+
    geom_errorbar(aes(x=label,ymin=100*(unviewed_percent-1.96*std_error),ymax=100*(unviewed_percent+1.96*std_error)), width=0.7)+
    ylab("results unviewed (%)")+
    xlab(NULL)+
    expand_limits(y=c(0,15))
  )
}

ttvPlot <- function(groupedByLabelled) {
  return(
    ggplot(groupedByLabelled %>% 
             filter(viewed & minutes_to_view > 0)
    )+
    geom_violin(aes(x=label,y=minutes_to_view+1), position="dodge", colour="black", fill="#c0c0c0")+
    geom_boxplot(aes(x=label,y=minutes_to_view+1), width=0.2, fill="#8080ff", colour="blue")+
    ylab("time to view")+
    xlab(NULL)+
    scale_y_log10(breaks=c(2,60+1,(6*60)+1,(24*60)+1,(7*24*60)+1,(30*24*60)+1),labels=c("1m","1h","6h","1d","1w","30d"))
  )
}

ttvBoxPlot <- function(groupByLabelled) {
  return(
    ggplot(
      groupByLabelled %>% 
        filter(viewed & minutes_to_view > 0) %>%
        summarize(
          count=n(),
          median=fivenum(minutes_to_view)[3],
          std_error=1.2533*sd(minutes_to_view)/sqrt(n()),
        ), aes(x=label))+
    geom_bar(aes(y=median),stat="identity",width=0.5, colour="black",fill="#8080ff")+
    geom_errorbar(aes(ymin=(median-1.96*std_error),ymax=(median+1.96*std_error)), width=0.4)+
    ylab("median ttv (mins)")+
    xlab(NULL)+
    scale_y_continuous(breaks=seq(0,6000,30))+#,limits=c(0,360))
    expand_limits(y=0)
  )
}

#TODO: Think about a non log plot for boxplot
# unviewedPlot(data %>% group_by(label=discipline_name))+expand_limits(y=60)+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))
# ttvPlot(data %>% group_by(label=discipline_name))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))
# ttvBoxPlot(data %>% group_by(label=discipline_name))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))

unviewedPlot(data_baseline %>% group_by(label = discreteNumericsToFactors(emis,8)))+xlab("emis identifiers")
ttvPlot(data_baseline %>% group_by(label = discreteNumericsToFactors(emis,8)))+xlab("emis identifiers")
ttvBoxPlot(data_baseline %>% group_by(label = discreteNumericsToFactors(emis,8)))+xlab("emis identifiers")

unviewedPlot(data_baseline %>% group_by(label=ifelse(patient_id_updated==1,"changed id","original id")))
ttvPlot(data_baseline %>% group_by(label=ifelse(patient_id_updated==1,"changed id","original id")))
ttvBoxPlot(data_baseline %>% group_by(label=ifelse(patient_id_updated==1,"changed id","original id")))

unviewedPlot(data_baseline %>% group_by(label=normaliseText(dependency_level)))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))
ttvPlot(data_baseline %>% group_by(label=normaliseText(dependency_level)))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))
ttvBoxPlot(data_baseline %>% group_by(label=normaliseText(dependency_level)))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))

unviewedPlot(data_baseline %>% group_by(label=normaliseText(patient_group)))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))
ttvPlot(data_baseline %>% group_by(label=normaliseText(patient_group)))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))
ttvBoxPlot(data_baseline %>% group_by(label=normaliseText(patient_group)))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))

unviewedPlot(data_baseline %>% group_by(label=normaliseText(patient_gender)))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))
ttvPlot(data_baseline %>% group_by(label=normaliseText(patient_gender)))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))
ttvBoxPlot(data_baseline %>% group_by(label=normaliseText(patient_gender)))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))

  # theme(legend.justification=c(1,1), legend.position=c(0.95,0.95), legend.background = element_rect(colour="black",size=.5, linetype="dashed"))+
  # scale_y_continuous(limits=c(0,10)) #+
  #scale_fill_manual(name=NULL,
  #                  breaks=c(TRUE,FALSE), values=c("#ff8080","#ffffff"),))
  #                  labels=c("Viewed", "Not viewed"))+xlab("count of internal ids")+ylab("proportion")

# mv_viewed_2 <- ggplot(data_baseline %>% 
#          mutate(emis_cat = cut(emis,breaks=c(-Inf,0.5,1.5,2.5,3.5,4.5,Inf), labels = c("0","1","2","3","4","5+"))) %>%
#         group_by(viewed, emis_cat) %>% 
#          summarize(count=n()) %>% 
#          mutate(freq = count / sum(count)), 
#        )+
#   geom_bar(aes(x=emis_cat,y=freq,fill=viewed), stat="identity", position="dodge", colour="black", width=0.8)+
#   theme(legend.justification=c(1,1), legend.position=c(0.95,0.95), legend.background = element_rect(colour="black",size=.5, linetype="dashed"))+
#   scale_y_continuous(limits=c(0,1))+
#   scale_fill_manual(name=NULL,
# breaks=c(TRUE,FALSE), values=c("#ff8080","#ffffff"),
# labels=c("Viewed", "Not viewed"))+xlab("count of internal ids")+ylab("proportion")

mv_viewed_3 <- ggplot(data %>% 
                        group_by(viewed, patient_id_updated) %>% 
                        summarize(count=n()) %>% 
                        mutate(freq = count / sum(count),
                               label=ifelse(patient_id_updated==1,"Updated id","Original id")
                               )
)+
  geom_bar(aes(x=label,y=freq,fill=viewed), stat="identity", position="dodge", colour="black", width=0.8)+
  theme(legend.justification=c(1,1), legend.position=c(0.95,0.95), legend.background = element_rect(colour="black",size=.5, linetype="dashed"))+
  scale_y_continuous(limits=c(0,1))+
  scale_fill_manual(name=NULL,
                      breaks=c(TRUE,FALSE),
                     values=c("#ff8080","#ffffff"),
                      labels=c("Viewed", "Not viewed"))+xlab(NULL)+ylab("proportion");

ggsave('viewedVersusIdentifiers.png',width=10,height=5,units='in');

# maxY = log10(max(data_baseline$minutes_to_view, na.rm = TRUE));
# maxX = max(data_baseline$publish_rate);
# 
# tmp <- data_baseline %>% filter(viewed & minutes_to_view>0) %>% 
#   mutate(
#     ygroup = floor(log10(minutes_to_view+10)/maxY*100),
#     xgroup = floor(publish_rate/maxX*100)
#   ) %>%
#   group_by(ygroup,xgroup) %>%
#   summarise(
#     fill=n()
#   ) %>% mutate(
#     xmin=xgroup*maxX/100,
#     xmax=(xgroup+1)*maxX/100,
#     ymin=ygroup*maxY/100,
#     ymax=(ygroup+1)*maxY/100,
#   )
# 
# ggplot(tmp)+
#   # geom_density2d()+
#   # geom_raster(interpolate=TRUE)+
#   geom_rect(aes(xmin=xmin,xmax=xmax,ymin=ymin,ymax=ymax,fill=fill))+
#   # scale_x_continuous(limits=c(0,7))+
#   # scale_x_log10()+
#   # scale_y_log10()+
#   scale_fill_continuous(low="#f8f8f8",high="#000000", labels = function(x) {return(10^(x-1))})
# publish rate
mv_viewed_1 <- ggplot(data_baseline, aes(x=publish_rate, alpha=viewed))+
  geom_density(fill="#ff0000")+
  ##scale_x_log10()+
  #scale_x_continuous(limits=c(0,7))+
  theme(legend.justification=c(1,1), legend.position=c(0.95,0.95), legend.background = element_rect(colour="black",size=.5, linetype="dashed"))+
  scale_alpha_discrete(name=NULL,
                       breaks=c(TRUE,FALSE),
                       labels=c("Viewed", "Not viewed"),
                       range=c(0.5,0))+
  xlab("concurrent publication rate (per minute)")+ylab("density")

## OK - working as good as it is going to get now.
ggplot( data_baseline %>% filter(viewed & minutes_to_view>0), aes(x=publish_rate,y=minutes_to_view+9)) +
  geom_bin2d(bins=100)+
  scale_y_log10(breaks=c(10,69,(6*60+9),(24*60+9),(7*24*60)+9),labels=c("1m","1h","6h","1d","1w"))+
  scale_fill_continuous(low="#f8f8f8",high="#000000")+
  xlab("publication rate (per min)")+
  ylab("time to view")+
  theme(legend.justification=c(1,1), legend.position=c(0.95,0.95), legend.background = element_rect(colour="black",size=.5, linetype="dashed"))



ggplot(
  data %>% 
    mutate(emis_cat = cut(emis,breaks=c(-Inf,0.5,1.5,2.5,3.5,4.5,5.5,6.5,Inf), labels = c("0","1","2","3","4","5","6","7+"))) %>% 
    group_by(viewed, emis_cat) %>% 
    summarize(count=n()) %>% 
    mutate(freq = count / sum(count)), 
  aes(x=viewed, fill=viewed, y=emis_cat)
  )+
  geom_tile(aes(width=sqrt(freq)*0.95, height=sqrt(freq)*0.95), colour="black")+
  coord_flip()

ggplot(
  data %>% group_by(viewed, nhsnos) %>% summarize(count=n()) %>% mutate(freq = count / sum(count)), 
  aes(x=viewed, fill=viewed, y=nhsnos)
)+
  geom_tile(aes(width=freq, height=0.9), colour="black")+
  coord_flip()

ggplot(
  data %>% group_by(viewed,patient_id_updated) %>% summarize(count=n()) %>% mutate(freq = count / sum(count)), 
  aes(x=viewed, y=patient_id_updated, fill=viewed)
)+
  geom_tile(aes(width=sqrt(freq)*0.95, height=sqrt(freq)*0.95), colour="black")+
  coord_flip()
ggsave('viewedVersusIdentifierUpdates.png',width=5,height=5,units='in')

# +scale_y_discrete(limits=c(0,15))
#  geom_boxplot(width=0.1, fill="grey")
#+scale_y_continuous(limits=c(0,7))



pm <- ggpairs(data,
              columns=c("minutes_to_view", "dependency_level", "emis", "nhsnos", "mrns", "patient_id_updated"),
              mapping=aes(colour=viewed)
              )



summary(data_baseline)
fivenum(data_baseline$minutes_to_view, na.rm = TRUE)
mean(data_baseline$minutes_to_view, na.rm = TRUE)
# percent tests viewed
100*(1-sum(ifelse(data_baseline$viewed,1,0))/count(data_baseline))
# percent abnormal tests viewed
100*(1-sum(ifelse(data_baseline$viewed & data_baseline$investigation_abnormal,1,0))/sum(ifelse(data_baseline$investigation_abnormal,1,0)))
# percent normal tests viewed 
100*(1-sum(ifelse(data_baseline$viewed & !(data_baseline$investigation_abnormal),1,0))/sum(ifelse(!data_baseline$investigation_abnormal,1,0)))

#n(data_baseline)/(12*3)
mean(data_baseline$total_views)
sd(data_baseline$total_views)


# cor(data$patient_age, data$minutes_to_view, use="complete.obs")
# cor(data$date, data$minutes_to_view, use="complete.obs")
# https://stats.stackexchange.com/questions/119835/correlation-between-a-nominal-iv-and-a-continuous-dv-variable/124618#124618
# model.lm <- lm(minutes_to_view ~ month_cat, data = data)

###########################################
# characterise data set

data_count <- nrow(data);
viewed_total <- nrow(data %>% filter(viewed))

# mean clinician views


# ttv distribution by hour
data_by_hour_to_view <- data %>%
  filter(minutes_to_view > 0) %>%
  group_by(minutes_to_view) %>%
  summarize(
    count=n()
  ) %>% mutate(
    cumulative=cumsum(count),
    recovered_percent=cumulative / data_count
  )

ttv_distribution_8_hours <- ggplot(data = data_by_hour_to_view %>% filter(minutes_to_view < 60*8),
       aes(x=minutes_to_view,y=count))+
  geom_hline(yintercept = viewed_total/data_count*30000, colour="#808080") +
  geom_bar(stat="identity",width=1, fill="#c0c0ff")+
  geom_line(aes(y=recovered_percent*30000))+
  xlab("minutes since result")+
  scale_x_continuous(breaks=seq(0,60*8,60))+
  ylab("tests viewed per minute")+
  scale_y_continuous(limits=c(0,30000),breaks = seq(0, 30000, 5000),
                     sec.axis=sec_axis(~./300, name="percent viewed")
  )
ttv_distribution_8_hours+ggtitle("Tests viewed in first 8 hours");
ggsave('timeToViewDistributionFirst8Hours.png',width=10,height=5,units='in')
ttv_distribution_8_hours;
ggsave('timeToViewDistributionFirst8HoursNoTitle.png',width=10,height=5,units='in')

ttv_distribution_1_day <- ggplot(data = data_by_hour_to_view %>% filter(minutes_to_view < 60*24),
       aes(x=minutes_to_view,y=count))+
  geom_hline(yintercept = viewed_total/data_count*30000, colour="#ff8080") +
  geom_area(stat="identity",fill="#8080ff")+
  geom_line(stat="identity",width=1, colour="#0000ff")+
  geom_line(aes(y=recovered_percent*30000))+
  xlab("minutes")+
  scale_x_continuous(breaks=seq(0,24*7*60,3*60))+
  ylab("count")+
  scale_y_continuous(limits=c(0,30000),breaks = seq(0, 30000, 5000),
                     sec.axis=sec_axis(~./300, name="percent viewed")
  )
ttv_distribution_1_day+ggtitle("Tests viewed in 24 hours");
ggsave('timeToViewDistribution1days.png',width=10,height=5,units='in')
ttv_distribution_1_day
ggsave('timeToViewDistribution1daysNoTitle.png',width=10,height=5,units='in')

clinician_views_distribution <- ggplot(data, aes(x=total_views)) +
geom_histogram(bins = max(data$total_views),fill="#ffb080",colour="black") +
xlab("no. clinicians")+
scale_x_continuous(breaks=seq(0,50,5))
clinician_views_distribution
ggsave('clinicianViewsDistribution.png',width=10,height=5,units='in')

distribution <- plot_grid(
  ttv_distribution_1_day,
  clinician_views_distribution,
  nrow=1,
  align="v",
  rel_widths=c(2,1),
  labels = c("A","B")
)
distribution

save_plot("distributionPub.png", distribution,base_height = 3,base_width = 10)

#############################

# distribution by month
data_by_month <- data_baseline %>%
  # filter(minutes_to_view < 31*24*60) %>%
  group_by(month_cat) %>%
  summarize(
    count=n(),
    viewed_percent=sum(ifelse(viewed,1,0))/n(),
    mean_views = mean(total_views),
    views_lower_fence=fivenum(total_views, na.rm = TRUE)[1],
    views_lower_quartile=fivenum(total_views, na.rm = TRUE)[2],
    views_median=fivenum(total_views, na.rm = TRUE)[3],
    views_upper_quartile=fivenum(total_views, na.rm = TRUE)[4],
    views_higher_fence=fivenum(total_views, na.rm = TRUE)[5],
    lower_fence=fivenum(minutes_to_view, na.rm = TRUE)[1],
    lower_quartile=fivenum(minutes_to_view, na.rm = TRUE)[2],
    median=fivenum(minutes_to_view, na.rm = TRUE)[3],
    upper_quartile=fivenum(minutes_to_view, na.rm = TRUE)[4],
    higher_fence=fivenum(minutes_to_view, na.rm = TRUE)[5]
  )

baseline_historical_time_to_view <- ggplot(data_by_month, aes(x=month_cat)) +
  geom_boxplot(
    aes(
      lower = lower_quartile,
      upper = upper_quartile,
      middle = median,
      ymin = 0,
      ymax = (upper_quartile+1.5*(upper_quartile-lower_quartile))
    ), 
    stat="identity", fill="#c0c0c0", size=0.3) +
  # theme(axis.text.x = element_text(angle=90,size=9,vjust=1,hjust=1))+
  xlab("month") +
  ylab("minutes") +
  scale_y_continuous(breaks = seq(0, 6000, 60)) +
  theme(axis.text.x = element_text(angle=90,size=9,hjust=1,vjust=1))
baseline_historical_time_to_view+ggtitle("median time to view chemistry and haematology results by calendar month")
ggsave('historicalTimeToViewTimeBoxPlot.png',width=10,height=5,units='in')

baseline_historical_percent_unviewed <- ggplot(data_by_month, aes(x=month_cat)) +
    geom_bar(
      aes(
        y= (1-viewed_percent)*100
      ), stat="identity", fill="#ffc0c0", colour="black", width=0.75, size=0.3) +
    ylab("percent")+
  xlab("month")+
    theme(axis.text.x = element_text(angle=90,size=9,hjust=1,vjust=1))+
    expand_limits(y=c(0,15))
baseline_historical_percent_unviewed+ggtitle("percent of chemistry and haematology results unviewed by calendar month")
  ggsave('historicalUnviewedTests.png',width=10,height=5,units='in')

  
  baseline_historical_clinicians_viewing <- ggplot(data_by_month, aes(x=month_cat)) +
    #geom_boxplot(
    #  aes(
    #    lower = views_lower_quartile,
    #    upper = views_upper_quartile,
    #    middle = views_median,
    #    ymin = views_lower_fence,
    #    ymax = (views_upper_quartile+1.5*(views_upper_quartile-views_lower_quartile))
    #  ), 
    #  stat="identity", fill="#c0c0ff", size=0.3) +
    geom_bar(
      aes(
        y= mean_views
      ), stat="identity", fill="#c0c0ff", colour="black", width=0.75, size=0.3) +
    ylab("clinicians") +
    xlab("month") +
    theme(axis.text.x = element_text(angle=90,size=9,hjust=1,vjust=1)) +
    scale_y_continuous(limits=c(0,5),breaks = seq(0, 5, 1)) 
  baseline_historical_clinicians_viewing+ggtitle("unique clinicians viewing chemistry and haematology results by calendar month")
  ggsave('historicalClinicianViewers.png',width=10,height=5,units='in')

  baseline_historical_time_to_view
  baseline_historical_percent_unviewed
  baseline_historical_clinicians_viewing
  
  baseline <- plot_grid(
    baseline_historical_time_to_view+rremove("x.text")+rremove("xlab"),
    baseline_historical_percent_unviewed+rremove("x.text")+rremove("xlab"),
    baseline_historical_clinicians_viewing,
    nrow=3,
    align="v",
    labels = c("A","B","C")
  )
  
  baseline
  save_plot("baselinePub.png", baseline,base_height = 10,base_width = 10)
  
  ###################################
  ## TODO: more fiddling with enquo
  #########################################
  # Detailed correlations
  # https://cran.r-project.org/web/packages/dplyr/vignettes/programming.html
  plot_bubble  <- function(input,grouper,bubble_colour) {
    grouper <- enquo(grouper);
    bubble_colour <- enquo(bubble_colour);
    tmpCorrelations <- input %>%
      group_by(!! grouper, !! bubble_colour)%>%
      dplyr::summarize(
        count=n(),
        median=fivenum(minutes_to_view, na.rm = TRUE)[3],
        viewed_percent=sum(total_views != 0)/n()*100
      );  
    # tmpCorrelations <- mutate(tmpCorrelations,
    #     the_label := !!grouper,
    #     the_colour := !!bubble_colour # the colon before the !!enquo is essential to stop R interpreting this as a 
    #   );
    tmpCorrelations <- tmpCorrelations %>% ungroup() %>% top_n(40,count)
    View(tmpCorrelations)
    return(ggplot(
      tmpCorrelations,
      aes(x=median,y=viewed_percent,label:=!!grouper,size=count))+
        geom_point(shape=21, aes(fill:=!!bubble_colour), colour="black")+
        scale_size_area(max_size=20)+
        geom_text_repel(size=3, segment.colour="#00000080") +
        ylab("percent viewed") +
        xlab("median time to view result (minutes)")+
        expand_limits(x = 0, y=100) +#, y=c(-0.15,0.25))+
        guides(fill=FALSE, size=FALSE)+
        scale_x_continuous(breaks=seq(0,12000,60)))
  }
  
  plot_bubble(data,discipline_name,investigation_abnormal)+ggtitle("Time to view and percent viewed by discipline")+
    scale_x_continuous(limits=c(0,3000),breaks=seq(0,12000,600))
  ggsave('timeToViewAndPercentViewedByDiscipline.png',width=10,height=5,units='in')
  plot_bubble(data_baseline,investigation_name,"#ff808080")+ggtitle("Time to view and percent viewed by test")+
    scale_x_continuous(limits=c(0,300),breaks=seq(0,12000,60))+scale_fill_identity()
  ggsave('timeToViewAndPercentViewedByTest.png',width=10,height=5,units='in')
  plot_bubble(data_baseline,ward_name,dependency_level)+ggtitle("Time to view and percent viewed by ward")+scale_fill_brewer(palette="RdYlBu")
  ggsave('timeToViewAndPercentViewedByWard.png',width=10,height=5,units='in')
  plot_bubble(data_baseline,patient_group,"#8080ff80")+ggtitle("Time to view and percent viewed by patient group")+scale_fill_identity()
  ggsave('timeToViewAndPercentViewedByPatientGroup.png',width=10,height=5,units='in')
  plot_bubble(data_baseline %>%
                mutate(age_group=patient_age %/% 10 * 10),age_group,"#d0d08080")+ggtitle("Time to view and percent viewed by patient decade")
  ggsave('timeToViewAndPercentViewedByPatientDecade.png',width=10,height=5,units='in')
  
  
  ######################################
  
    tmpCorrelations <- data %>%
      group_by(discipline_name, investigation_abnormal) %>%
      dplyr::summarize(
        count=n(),
        median=fivenum(minutes_to_view, na.rm = TRUE)[3],
        viewed_percent=sum(total_views != 0)/n()*100
      ) %>% 
      mutate(
        the_label=paste0(discipline_name,ifelse(investigation_abnormal>0," (a)"," (n)")),
        the_colour=investigation_abnormal
      );
    tmpCorrelations <- tmpCorrelations %>% ungroup() %>% top_n(40,count)
    ggplot(
      tmpCorrelations %>% arrange(desc(the_colour)),
      aes(x=median,y=viewed_percent,label=the_label,size=count))+
        geom_point(shape=21, aes(fill=the_colour), colour="black", alpha=0.5)+
        scale_size_area(max_size=30)+
        geom_text_repel(
          size=3.5,
          nudge_x = 45,
          box.padding = unit(0.1,"inch"),
          point.padding = unit(0.1,"inch"), 
          # Color of the line segments.
          segment.color = '#606060',
          segment.size = 0.5,
          # min.segment.length = unit(0.5,"inch"),
          arrow = arrow(length = unit(0.05, 'inch')),
          force = 1,
        ) +
        ylab("percent viewed") +
        xlab("median time to view result (minutes)")+
        expand_limits(x = 0, y=100) +#, y=c(-0.15,0.25))+
        guides(fill=FALSE, size=FALSE)+
      ggtitle("Time to view and percent viewed by discipline")+
      scale_x_continuous(limits=c(0,3000),breaks=seq(0,12000,600))+
      scale_fill_continuous(low="#4040ff",high="#ff4040")
    ggsave('timeToViewAndPercentViewedByDiscipline.svg',width=5,height=3,units='in')
  
  ##############################
    
    tmpCorrelations <- data_baseline %>%
      group_by(investigation_name) %>%
      dplyr::summarize(
        count=n(),
        median=fivenum(minutes_to_view, na.rm = TRUE)[3],
        viewed_percent=sum(total_views != 0)/n()*100
      ) %>% 
      mutate(
        the_label=investigation_name
      );
    
    ggplot(
      tmpCorrelations,
      aes(x=median,y=viewed_percent,label=the_label))+
      geom_point(shape=21, colour="black", fill="#ff8080", alpha=0.5,size=2)+
      geom_text_repel(
        data=tmpCorrelations %>% ungroup() %>% top_n(20,count),
        size=2,
        segment.color = '#00000040'
      ) +
      ylab("percent viewed") +
      xlab("median time to view result (minutes)")+
      expand_limits(x = 0, y=100) +#, y=c(-0.15,0.25))+
      guides(fill=FALSE, size=FALSE)+
      ggtitle("Time to view and percent viewed by test")+
      scale_x_continuous(limits=c(0,300),breaks=seq(0,300,60))+
      scale_y_continuous(limits=c(70,100),breaks=seq(0,100,10))
    ggsave('timeToViewAndPercentViewedByTest.svg',width=5,height=3,units='in')
    
    ggplot(
      data_baseline %>% inner_join(tmpCorrelations %>% ungroup() %>% top_n(30,count),  by="investigation_name"), 
           aes(x=reorder(investigation_name,-viewed_percent), y=minutes_to_view))+
      geom_violin(aes(fill=count),trim=FALSE)+ #, fill="#c0c0c0")+
      geom_boxplot(width=0.25, fill="#8080ff", outlier.size = 0.5)+
      theme(axis.text.x = element_text(angle=60,size=8,hjust=1,vjust=1)) +
      # coord_flip()+
      scale_y_log10(labels = comma,breaks=c(1,10,100,1000,10000,100000))+
      ylab("time to view (mins)")+
      xlab(NULL)+
      guides(fill=FALSE, size=FALSE)+
      scale_fill_continuous(low="#f0f0f0",high="#404040")
    ggsave('timeToViewByTest.svg',width=7*1.2,height=4*1.2,units='in')
    ggsave('timeToViewByTest.png',width=7,height=4,units='in')
    
    ggplot(
      tmpCorrelations %>% ungroup() %>% top_n(30,count), 
      aes(x=reorder(investigation_name,-viewed_percent), y=100-viewed_percent))+
      geom_bar(width=0.5, fill="#ff8080", colour="black", stat="identity")+
      theme(axis.text.x = element_text(angle=60,size=8,hjust=1,vjust=1)) +
      ylab("percent unviewed results")+
      xlab(NULL)
      
    ggsave('unviewedByTest.svg',width=7*1.2,height=3*1.2,units='in')
    ggsave('unviewedByTest.png',width=7,height=3,units='in')
    
    ##############################
    
    tmpCorrelations <- data_baseline %>%
      group_by(ward_name,dependency_level) %>%
      dplyr::summarize(
        count=n(),
        median=fivenum(minutes_to_view, na.rm = TRUE)[3],
        viewed_percent=sum(total_views != 0)/n()*100
      ) %>% 
      mutate(
        the_label=ward_name,
        the_colour=dependency_level
      );
    tmpCorrelations <- tmpCorrelations %>% ungroup() %>% top_n(40,count)
    ggplot(
      tmpCorrelations,
      aes(x=median,y=viewed_percent,label=the_label,size=count))+
      geom_point(shape=21, aes(,fill=the_colour), colour="black", alpha=0.5)+
      scale_size_area(max_size=18)+
      geom_text_repel(
        size=2.5,
        segment.color = '#00000040',
        force=1,
        box.padding=0.25
      ) +
      ylab("percent viewed") +
      xlab("median time to view result (minutes)")+
      expand_limits(x = 0, y=100) +#, y=c(-0.15,0.25))+
      guides(fill=FALSE, size=FALSE)+
      ggtitle("Time to view and percent viewed by ward")+
      scale_x_continuous(limits=c(0,200),breaks=seq(0,200,60))
    ggsave('timeToViewAndPercentViewedByWard.svg',width=5,height=3,units='in')