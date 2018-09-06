# install.packages("car")
# devtools::install_github("thomasp85/patchwork")

library(DBI)
library(odbc)
library(datasets)
library(signal)
# library(boot)

library(rlang)
library(reshape2)
library(GGally)
library(cowplot)
library(scales)
library(ggrepel)

library(ggpubr)
library(patchwork)
library(broom)
library(vcd) # cramer v
library(heplots)  # anova / etasq
library(boot)
detach("package:dplyr", unload=TRUE)
library(dplyr)

# source_directory <- getSrcDirectory(function(dummy) {dummy});
# if (source_directory == "") source_directory="/home/robchallen/Git/working-drafts/src/main/r"
# source(paste(source_directory,'/standardAxes.R',sep=""));
# source(paste(source_directory,'/utils.R',sep=""));
# createDirectory();

setwd("~/Dropbox/ECMM433 data science/tmp")

pwd <- readline(prompt="Enter DB: ");
con <- dbConnect(odbc(),
                 Driver = "ODBC Driver 13 for SQL Server",
                 Server = "10.174.129.118",
                 Database = "RobsDatabase",
                 UID = "RobertSQL",
                 PWD = pwd,
                 Port = 1433);

dbListTables(con);

data <- dbReadTable(con, "aggTimeToView", as.is=c(FALSE,TRUE,FALSE,FALSE,FALSE,FALSE,FALSE,FALSE,TRUE,TRUE,FALSE,FALSE,FALSE,FALSE,
                                                  FALSE,FALSE,FALSE,FALSE,FALSE,FALSE,FALSE,FALSE));

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
pub_rate <- pub_rate %>% left_join(data %>% group_by(date=first_viewed_date) %>% summarise(views=n())) 
pub_rate <- pub_rate %>% left_join(data %>% group_by(date=specimen_date) %>% summarise(specimens=n()))
pub_rate <- pub_rate %>% mutate(
  events = ifelse(is.na(events),0,events),
  views = ifelse(is.na(views),0,views),
  specimens = ifelse(is.na(specimens),0,specimens)
)
pub_rate <- pub_rate %>% mutate(
  cumEvents = cumsum(events),
  cumViews = cumsum(views),
  cumSpecimens = cumsum(specimens)
)
width = 61
pub_rate <- pub_rate %>% mutate(
  smoothedEvents = as.numeric(stats::filter(cumEvents,filter=rep(1/width,width),method='convolution',sides=2,circular=FALSE)),
  smoothedViews = as.numeric(stats::filter(cumViews,filter=rep(1/width,width),method='convolution',sides=2,circular=FALSE)),
  smoothedSpecimens = as.numeric(stats::filter(cumSpecimens,filter=rep(1/width,width),method='convolution',sides=2,circular=FALSE))
) %>% filter( !is.na(smoothedEvents) & !is.na(smoothedViews) & !is.na(smoothedSpecimens))
pub_rate <- pub_rate %>% mutate(
  publish_rate = sgolayfilt(smoothedEvents,m=1),
  viewed_rate = sgolayfilt(smoothedViews,m=1),
  specimen_rate = sgolayfilt(smoothedSpecimens,m=1)
) %>% select(date,publish_rate,viewed_rate,specimen_rate)

pub_rate <- pub_rate %>% mutate(
  minute_of_day = as.numeric(format(date, "%H"))*60+as.numeric(format(date, "%M")),
  day_of_week = as.numeric(format(date, "%u")) # monday = 1, sunday = 7
)

 #+
  # geom_line(aes(y=specimen_rate), colour = "#ff0000", size=1)

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
data <- data %>% inner_join(pub_rate %>% select(date,publish_rate),by="date")

data <- data %>% mutate(
  days_processing = minutes_processing/60/24,
  patient_decade = patient_age/10
)

data <- data %>% filter(
  as.Date(date) <= as.Date("2017-08-31") 
  & ward_name != "Other"
  & as.Date(date) >= as.Date("2014-09-01")
);

data <- data %>% mutate(month_cat=format(date, "%Y-%m")) %>%
  mutate(
    time_of_day = as.numeric(format(date, "%H"))*60+as.numeric(format(date, "%M")),
    day_of_week = as.numeric(format(date, "%u")), # monday = 1, sunday = 7
    time_of_week = (day_of_week-1)*24*60+time_of_day,
    # time_of_week_viewed=as.numeric(difftime(first_viewed_date,cut(first_viewed_date,"week"),unit="mins"))%%(24*7*60)) %>%
    # time_of_day_viewed=time_of_week_viewed %% (24*60)) %>%
    end_month_cat=as.factor(format(first_viewed_date, "%Y-%m"))
  );

data <- data %>%  mutate(day_time_cat = cut(
  (time_of_day+16*60)%%(24*60),
  breaks=c(0,4*60,8*60,10*60,25*60),
  labels=c("08:00-11:59","12:00-15:59","16:00-17:59","18:00-07:59"),
  ordered_result=FALSE,
  include.lowest=TRUE
));

data <- data %>% filter(discipline_name %in% c(
  "Blood Transfusion",
  "Chem/Haem",
  "Microbiology",
  "Radiology",
  "Virology"))

data <- data %>% mutate(
  investigation_name = as.factor(investigation_name),
  discipline_name = as.factor(discipline_name),
  investigation = as.factor(investigation),
  investigation_name = as.factor(investigation_name),
  investigation_abnormal = ifelse(investigation_abnormal == 1, TRUE, FALSE),
  ward_name = as.factor(ward_name),
  dependency_level = as.factor(dependency_level),
  view_type = as.factor(view_type),
  patient_gender = as.factor(patient_gender),
  patient_id_updated =  ifelse(patient_id_updated == 1, TRUE, FALSE),
  same_graph = ifelse(same_graph == 1, TRUE, FALSE),
  tsft_test = ifelse(tsft_test == 1, TRUE, FALSE),
  viewed = ifelse(viewed == 1, TRUE, FALSE)
) 

# summary(lm(viewed~minutes_processing+investigation_name+minutes_processing*investigation_name, data_baseline))
# summary(lm(viewed~minutes_processing+investigation_name+minutes_processing*investigation_name, data_baseline %>% sample_n(10000)))

data <- data %>% mutate(weekday_cat = as.factor(ifelse(day_of_week<5.5, "Mon-Fri","Sat-Sun")))

data_baseline <- data %>% filter(
  investigation_abnormal
  ) %>% filter(
    discipline_name=='Chem/Haem'
  ) %>% filter(
    as.Date(date) < as.Date("2017-09-01")
    & as.Date(date) >= as.Date("2014-09-01")
  ) %>% filter(
   dependency_level != 'PRIVATE'
  ) 

data_baseline <- data_baseline %>%  filter(is.na(minutes_to_view) | minutes_to_view >= 0);


rm(pwd);

#################################
# Baseline characteristics

view_df <- function(x){
  tf <- tempfile(fileext = ".html")
  writeLines(
    print(
      xtable(x),type="html", html.table.attributes=""
    )
    , tf)
  rstudioapi::viewer(tf)
}

count(data,discipline_name) %>% mutate(percent = sprintf("%2.2f%%",n/sum(n)*100))
count(data,investigation_abnormal) %>% mutate(percent = sprintf("%2.2f%%",n/sum(n)*100))

view_df(count(data_baseline,dependency_level) %>% mutate(percent = sprintf("%2.2f%%",n/sum(n)*100)))
view_df(count(data_baseline,patient_id_updated) %>% mutate(percent = sprintf("%2.2f%%",n/sum(n)*100)))
view_df(count(data_baseline,patient_gender) %>% mutate(percent = sprintf("%2.2f%%",n/sum(n)*100)))
view_df(count(data_baseline,day_time_cat) %>% mutate(percent = sprintf("%2.2f%%",n/sum(n)*100)))
view_df(count(data_baseline,weekday_cat) %>% mutate(percent = sprintf("%2.2f%%",n/sum(n)*100)))

View(count(data_baseline,dependency_level) %>% mutate(percent = sprintf("%2.2f%%",n/sum(n)*100)))

################################


# theme_set(theme_cowplot(font_size = 8*90/72))

saveGraph <- function(filename,vert_aspect,cols=1,plot) {
  if(cols==1) w=89 else w=183
  theme_set(theme_cowplot(font_size = 8*90/72))
  cowplot::save_plot(paste0(filename,".svg"), plot,base_height = w*90/72*vert_aspect,base_width = w*90/72,units="mm")
  theme_set(theme_cowplot(font_size = 8))
  cowplot::save_plot(paste0(filename,".png"), plot,base_height = w*vert_aspect,base_width = w,units="mm")
}

day_scale <- scale_x_continuous(breaks=c(0,6,12,18,24)*60, labels=sprintf("%02d:00",c(0,6,12,18,24)));
week_scale <- scale_x_continuous(breaks=seq(1,8)*24*60, labels=c("Mon","Tue","Wed","Thur","Fri","Sat","Sun","Mon"));
textAngle <- function(degree, size=1) {return(theme(axis.text.x = element_text(angle=degree,size=rel(size),hjust=1,vjust=1)))}
intLegend <- theme(legend.justification=c(1,1), legend.position=c(0.95,0.95), legend.background = element_rect(fill = "white",colour="black",size=.5, linetype="dashed"))
# For guidance, Nature's standard figure sizes are 89 mm wide (single column) and 183 mm wide (double column). 
# The full depth of a Nature page is 247 mm. Figures can also be a column-and-a-half where necessary (120–136 mm).

#################################

# What is std error of median for our baseline distribution compared to ?

# library(boot)
# # function to obtain R-Squared from the data 
# med <- function(data, indices) {
#   d <- data %>% slice(indices) # allows boot to select sample 
#   return(median(d$minutes_to_view))
# } 
# tmp <- data_baseline %>% filter(viewed) %>% sample_n(10000)
# # bootstrapping with 1000 replications 
# results <- boot(data=
#     tmp,
#     statistic=med, R=1000, simple=TRUE)
# 
# # view results
# results 
# plot(results)
# 
# # get 95% confidence interval 
# ci <- boot.ci(results, conf=0.95, type="norm")
# ci
# sd(results$t)

tmp1 <- data_baseline %>% filter(viewed) %>% select(minutes_to_view)
write.csv(tmp1$minutes_to_view, file="~/Dropbox/minutes_to_view.txt")
rm(tmp1)
source("~/Dropbox/confidenceLimitModel.R")
saveGraph(filename="bootstrapConfidenceIntervals",vert_aspect=1,plot=fit_plot)
rm(bootstrap,bootsummary,con,fit_plot,ttv,dist_mean,dist_med,dist_se,width)


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

save.image(file="~/R/timeToViewData");
load(file="~/R/timeToViewData");


################################
# Rate plots

ratePlot <- function(groupedByTime) {
return(ggplot(groupedByTime %>%
         summarise(
           viewed_rate = mean(viewed_rate),
           viewed_rate_err = sd(viewed_rate),
           publish_rate = mean(publish_rate),
           publish_rate_err = sd(publish_rate),
           specimen_rate = mean(specimen_rate),
         ), aes(x=time))+
  geom_ribbon(aes(ymin=publish_rate-1.96*publish_rate_err, ymax=publish_rate+1.96*publish_rate_err), fill = "#c000c0", alpha=0.2)+
  geom_ribbon(aes(ymin=viewed_rate-1.96*viewed_rate_err, ymax=viewed_rate+1.96*viewed_rate_err), fill = "#0000ff", alpha=0.2)+
  geom_line(aes(y=publish_rate, colour = "published"), size=1)+
  geom_line(aes(y=viewed_rate, colour="viewed"), size=1)+
  scale_colour_manual(values=c("viewed"="#0000ff","published"="#c000c0"),name=NULL)+
  theme(legend.justification=c(1,1), legend.position=c(0.95,0.95), legend.background = element_rect(colour="black",size=.5, linetype="dashed"))+
  ylab("results per min")+
  xlab(NULL))+
  scale_y_continuous(limits=c(0,7))
}



# saveGraph(filename="rateWeekday",vert_aspect=0.5,plot=
a <- ratePlot(pub_rate %>% filter(day_of_week<=5) %>% group_by(time = (minute_of_day)))+
  day_scale+expand_limits(y=7)
# )

b <- ratePlot(pub_rate %>% filter(day_of_week>5) %>% group_by(time = (minute_of_day)))+
  day_scale+expand_limits(y=7)

saveGraph(filename="rateDay",vert_aspect=1,cols=1,plot=
  plot_grid(a,b,align="hv",nrow=2,ncol=1,labels="AUTO")
)

saveGraph(filename="rateWeek",vert_aspect=0.3,cols=2,plot=
  ratePlot(pub_rate %>% group_by(time = (minute_of_day+day_of_week*24*60)))+
  week_scale+expand_limits(y=7)
)

# ABSOLUTE OVER YEARS
t <- ggplot(data_baseline %>% 
              # filter(date>'2014-09-31 00:00' & date < '2017-10-01 00:00') %>% 
              group_by(day=as.Date(trunc(date,"days"))) %>% summarise(count=n())
       , aes(x=day,y=count, colour=ifelse(as.numeric(format(day,"%u"))>5,"weekend","weekday")))+
  geom_point(stat="identity", size=0.5)+ 
  geom_smooth(method='lm',formula=y~x)+ 
  scale_x_date(date_labels = "%d %b %y",breaks=seq(as.Date('2014-09-01'), as.Date('2017-09-01'), "6 month"))+
  textAngle(30)+xlab(NULL)+ylab("results per day")+scale_color_manual(name=NULL,values=c("weekend"="#c040c0","weekday"="#40c0c0"))
  
saveGraph(filename="overtime",vert_aspect=0.5,cols=1,plot=t)

##################################
# Deal with issues of category and abnormality
##################################

a <- ggplot(data %>% group_by(label=discipline_name, fill=ifelse(investigation_abnormal,"abnormal","normal")) %>%
         filter(viewed & minutes_to_view > 0))+
  geom_violin(aes(x=label,y=minutes_to_view+1, fill=fill), position="dodge", colour="black")+
  # geom_boxplot(aes(x=label,y=minutes_to_view+1, fill=fill), position="dodge", width=0.4, colour="blue")+
  ylab("time to view")+
  xlab(NULL)+
  scale_fill_manual(values=c("normal"="#b0b0b0","abnormal"="#606060"), name=NULL)+
  scale_y_log10(breaks=c(2,60+1,(6*60)+1,(24*60)+1,(7*24*60)+1,(28*24*60)+1),labels=c("1m","1h","6h","1d","1w","4w"))+
  textAngle(30)

b <- ggplot(data %>% group_by(label=discipline_name, fill=ifelse(investigation_abnormal,"abnormal","normal")) %>% 
         summarize(
           count=n(),
           unviewed_percent=sum(ifelse(viewed,0,1))/n()
         ) %>% mutate (
           std_error = sqrt(unviewed_percent*(1-unviewed_percent)/count)
         ) )+
  geom_bar(aes(x=label,y=100*unviewed_percent,fill=fill), stat="identity", position="dodge", colour="black")+
  geom_errorbar(aes(x=label,ymin=100*(unviewed_percent-1.96*std_error),ymax=100*(unviewed_percent+1.96*std_error),fill=fill), position="dodge", width=0.7)+
  scale_fill_manual(values=c("normal"="#ffb0b0","abnormal"="#ff6060"), name=NULL)+
  ylab("results unviewed (%)")+
  xlab(NULL)+
  expand_limits(y=c(0,30))+
  textAngle(30)

c <- ggplot(data %>% group_by(label=discipline_name) %>%
         filter(viewed & minutes_processing > 0 & minutes_processing < 30*60*24))+
  geom_violin(aes(x=label,y=minutes_processing+1), fill="#c0c080", position="dodge", colour="black")+
  # geom_boxplot(aes(x=label,y=minutes_to_view+1, fill=fill), position="dodge", width=0.4, colour="blue")+
  ylab("time to process")+
  xlab(NULL)+
  scale_fill_manual(values=c("normal"="#b0b0b0","abnormal"="#606060"), name=NULL)+
  scale_y_log10(breaks=c(2,60+1,(6*60)+1,(24*60)+1,(7*24*60)+1,(28*24*60)+1),labels=c("1m","1h","6h","1d","1w","4w"))+
  textAngle(30)

saveGraph(filename="discipline1",vert_aspect=1.25,cols=1,plot=
            (a+rremove("xlab")+rremove("x.text")) + b + plot_layout(ncol = 1, heights = c(1,1))
)


saveGraph(filename="discipline2",vert_aspect=0.5,cols=1,plot=c)
count(data,"discipline_name")

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

# https://stats.stackexchange.com/questions/108007/correlations-with-unordered-categorical-variables


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
    expand_limits(y=0)
  )
}
# install.packages("vcd")
# library(vcd)
# k <- assocstats(xtabs(~as.factor(investigation_name) + as.factor(viewed), data=data))
# k$cramer
# Cramers V

ttvPlot <- function(groupedByLabelled) {
  return(
    ggplot(groupedByLabelled %>% 
             filter(viewed & minutes_to_view > 0)
    )+
    geom_violin(aes(x=label,y=minutes_to_view+1), position="dodge", colour="black", fill="#c0c0c0")+
    geom_boxplot(aes(x=label,y=minutes_to_view+1), width=0.2, fill="#8080ff", colour="blue")+
    ylab("time to view")+
    xlab(NULL)+
    scale_y_log10(breaks=c(2,60+1,(6*60)+1,(24*60)+1,(7*24*60)+1,(28*24*60)+1),labels=c("1m","1h","6h","1d","1w","4w"))
  )
}
# https://stats.stackexchange.com/questions/119835/correlation-between-a-nominal-iv-and-a-continuous-dv-variable/124618#124618
# model.aov <- aov(duration ~ topic, data = data.df)
# summary(model.aov)
# etasq(model.aov, partial = FALSE)
k<-etasq(aov(log(minutes_to_view+1) ~ as.factor(investigation_name), data=data_baseline %>% sample_n(10000)))


ttvBoxPlot <- function(groupByLabelled) {
  return(
    ggplot(
      groupByLabelled %>% 
        filter(viewed & minutes_to_view > 0) %>%
        summarize(
          count=n(),
          median=fivenum(minutes_to_view)[3],
          lower95=predictLowCI(minutes_to_view),
          upper95=predictHighCI(minutes_to_view)
        ), aes(x=label))+
    geom_bar(aes(y=median),stat="identity",width=0.5, colour="black",fill="#8080ff")+
    geom_errorbar(aes(ymin=lower95,ymax=upper95), width=0.4)+
    ylab("median ttv (mins)")+
    xlab(NULL)+
    scale_y_continuous(breaks=seq(0,6000,30))+#,limits=c(0,360))
    expand_limits(y=0)
  )
}

# unviewedPlot(data %>% group_by(label=discipline_name))+expand_limits(y=60)+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))
# ttvPlot(data %>% group_by(label=discipline_name))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))
# ttvBoxPlot(data %>% group_by(label=discipline_name))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))

# number fo patient identifiers
a<-ttvPlot(data_baseline %>% group_by(label = discreteNumericsToFactors(emis,8)))+xlab("emis identifiers")
b<-ttvBoxPlot(data_baseline %>% group_by(label = discreteNumericsToFactors(emis,8)))+xlab("emis identifiers")
c<-unviewedPlot(data_baseline %>% group_by(label = discreteNumericsToFactors(emis,8)))+xlab("emis identifiers")

saveGraph(filename="emis_identifiers",vert_aspect=1.25,cols=1,plot=
            (a+rremove("xlab")+rremove("x.text")) + (b+rremove("xlab")+rremove("x.text")) + c + plot_layout(ncol = 1, heights = c(3,2,2))
)

# number identifier changed
a<-ttvPlot(data_baseline %>% group_by(label=ifelse(patient_id_updated==1,"changed id","original id")))
b<-ttvBoxPlot(data_baseline %>% group_by(label=ifelse(patient_id_updated==1,"changed id","original id")))
c<-unviewedPlot(data_baseline %>% group_by(label=ifelse(patient_id_updated==1,"changed id","original id")))

saveGraph(filename="identifier_changed",vert_aspect=1.25,cols=1,plot=
            (a+rremove("xlab")+rremove("x.text")) + (b+rremove("xlab")+rremove("x.text")) + c + plot_layout(ncol = 1, heights = c(3,2,2))
)

# dependency level
a<-ttvPlot(data_baseline %>% group_by(label=normaliseText(dependency_level)))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))
b<-ttvBoxPlot(data_baseline %>% group_by(label=normaliseText(dependency_level)))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))
c<-unviewedPlot(data_baseline %>% group_by(label=normaliseText(dependency_level)))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))

saveGraph(filename="dependency_level",vert_aspect=1.25,cols=1,plot=
            (a+rremove("xlab")+rremove("x.text")) + (b+rremove("xlab")+rremove("x.text")) + c + plot_layout(ncol = 1, heights = c(3,2,2))
)

# patient group 
a<-ttvPlot(data_baseline %>% group_by(label=normaliseText(patient_group)))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))
b<-ttvBoxPlot(data_baseline %>% group_by(label=normaliseText(patient_group)))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))
c<-unviewedPlot(data_baseline %>% group_by(label=normaliseText(patient_group)))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))

saveGraph(filename="patient_group",vert_aspect=1.25,cols=1,plot=
            (a+rremove("xlab")+rremove("x.text")) + (b+rremove("xlab")+rremove("x.text")) + c + plot_layout(ncol = 1, heights = c(3,2,2))
)

# gender
a<-ttvPlot(data_baseline %>% group_by(label=normaliseText(patient_gender)))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))
b<-ttvBoxPlot(data_baseline %>% group_by(label=normaliseText(patient_gender)))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))
c<-unviewedPlot(data_baseline %>% group_by(label=normaliseText(patient_gender)))+theme(axis.text.x = element_text(angle=20,size=9,hjust=1,vjust=1))

saveGraph(filename="gender",vert_aspect=1.25,cols=1,plot=
            (a+rremove("xlab")+rremove("x.text")) + (b+rremove("xlab")+rremove("x.text")) + c + plot_layout(ncol = 1, heights = c(3,2,2))
)

# by month
a<-ttvPlot(data_baseline %>% group_by(label=format(date,"%m")))+
  scale_x_discrete(labels=c("01"="Jan","02"="Feb","03"="Mar","04"="Apr","05"="May","06"="Jun","07"="Jul","08"="Aug","09"="Sept","10"="Oct","11"="Nov","12"="Dec"))+
  theme(axis.text.x = element_text(angle=90,size=9,hjust=1,vjust=0.5))
b<-ttvBoxPlot(data_baseline %>% group_by(label=format(date,"%m")))+
  scale_x_discrete(labels=c("01"="Jan","02"="Feb","03"="Mar","04"="Apr","05"="May","06"="Jun","07"="Jul","08"="Aug","09"="Sept","10"="Oct","11"="Nov","12"="Dec"))+
  theme(axis.text.x = element_text(angle=90,size=9,hjust=1,vjust=0.5))
c<-unviewedPlot(data_baseline %>% group_by(label=format(date,"%m")))+
  scale_x_discrete(labels=c("01"="Jan","02"="Feb","03"="Mar","04"="Apr","05"="May","06"="Jun","07"="Jul","08"="Aug","09"="Sept","10"="Oct","11"="Nov","12"="Dec"))+
  theme(axis.text.x = element_text(angle=90,size=9,hjust=1,vjust=0.5))

saveGraph(filename="month",vert_aspect=1.25,cols=1,plot=
            (a+rremove("xlab")+rremove("x.text")) + (b+rremove("xlab")+rremove("x.text")) + c + plot_layout(ncol = 1, heights = c(3,2,2))
)

## By top n investigations
selector <- data_baseline %>% group_by(investigation_name) %>% summarise(
    count=n(),
    ttv=median(minutes_to_view,na.rm=TRUE)
  ) %>% arrange(ttv) %>% top_n(30,count) %>% mutate(
    label=factor(normaliseText(investigation_name),
    levels=normaliseText(investigation_name),
    ordered=TRUE)
  ) 


a<-ttvPlot(data_baseline %>% inner_join(selector) %>% group_by(label))+theme(axis.text.x = element_text(angle=45,size=9,hjust=1,vjust=1))
b<-ttvBoxPlot(data_baseline %>% inner_join(selector) %>% group_by(label))+theme(axis.text.x = element_text(angle=45,size=9,hjust=1,vjust=1))+scale_y_continuous(breaks=seq(0,600,60))
c<-unviewedPlot(data_baseline %>% inner_join(selector) %>% group_by(label))+theme(axis.text.x = element_text(angle=45,size=9,hjust=1,vjust=1))

saveGraph(filename="investigation_name",vert_aspect=1,cols=2,plot=
            (a+rremove("xlab")+rremove("x.text")) + (b+rremove("xlab")+rremove("x.text")) + c + plot_layout(ncol = 1, heights = c(3,2,2))
)



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

# mv_viewed_3 <- ggplot(data %>% 
#                         group_by(viewed, patient_id_updated) %>% 
#                         summarize(count=n()) %>% 
#                         mutate(freq = count / sum(count),
#                                label=ifelse(patient_id_updated==1,"Updated id","Original id")
#                                )
# )+
#   geom_bar(aes(x=label,y=freq,fill=viewed), stat="identity", position="dodge", colour="black", width=0.8)+
#   theme(legend.justification=c(1,1), legend.position=c(0.95,0.95), legend.background = element_rect(colour="black",size=.5, linetype="dashed"))+
#   scale_y_continuous(limits=c(0,1))+
#   scale_fill_manual(name=NULL,
#                       breaks=c(TRUE,FALSE),
#                      values=c("#ff8080","#ffffff"),
#                       labels=c("Viewed", "Not viewed"))+xlab(NULL)+ylab("proportion");
# 
# ggsave('viewedVersusIdentifiers.png',width=10,height=5,units='in');

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
# mv_viewed_1 <- ggplot(data_baseline, aes(x=publish_rate, alpha=viewed))+
#   geom_density(fill="#ff0000")+
#   ##scale_x_log10()+
#   #scale_x_continuous(limits=c(0,7))+
#   theme(legend.justification=c(1,1), legend.position=c(0.95,0.95), legend.background = element_rect(colour="black",size=.5, linetype="dashed"))+
#   scale_alpha_discrete(name=NULL,
#                        breaks=c(TRUE,FALSE),
#                        labels=c("Viewed", "Not viewed"),
#                        range=c(0.5,0))+
#   xlab("concurrent publication rate (per minute)")+ylab("density")

#######################
#CONTINUOUS VARIABLES
## OK - working as good as it is going to get now.

ttvHeatmap <- function(groupedData, grouper) {
  bins = n_groups(groupedData)-1
  grouper <- enquo(grouper);
  tmp <- groupedData %>% dplyr::filter(viewed & minutes_to_view>0)   %>% 
    summarise(
      x:=max(!!grouper),
      ymin=fivenum(minutes_to_view)[2],
      y=fivenum(minutes_to_view)[3],
      ymax=fivenum(minutes_to_view)[4]
    ) %>% mutate(
      ySmooth=sgolayfilt(cumsum(y),p=3,n=25,m=1),
      yminSmooth=sgolayfilt(cumsum(ymin),p=3,n=25,m=1),
      ymaxSmooth=sgolayfilt(cumsum(ymax),p=3,n=25,m=1)
    )
  return(ggplot() +
    geom_bin2d(
      data=groupedData %>% ungroup() %>% dplyr::filter(viewed & minutes_to_view>0) %>% mutate(x:=!!grouper), 
      mapping=aes(x=x,y=minutes_to_view+9),
      bins=c(bins,100))+
    scale_y_log10(breaks=c(10,69,(6*60+9),(24*60+9),(7*24*60)+9,(28*24*60)+9),labels=c("1m","1h","6h","1d","1w","4w"))+
    scale_fill_continuous(low="#ffffff",high="#000000",trans = "sqrt")+
    xlab(NULL)+
    ylab("time to view")+
    guides(fill=FALSE)+
    #theme(legend.justification=c(1,1), legend.position=c(0.95,0.95), legend.background = element_rect(colour="black", fill="white",size=.5, linetype="dashed"))+
    geom_line(
      data=tmp,
      mapping=aes(x=x,y=ySmooth+9),
      colour="#0000ff",
      size=1
    )+
    geom_line(
      data=tmp,
      mapping=aes(x=x,y=yminSmooth+9),
      colour="#0000ff",
      linetype="2121",
      size=1
    )+
    geom_line(
      data=tmp,
      mapping=aes(x=x,y=ymaxSmooth+9),
      colour="#0000ff",
      linetype="2121",
      size=1
    )
  )
}

ttvMedianContinuous <- function(groupedData, grouper) {
  grouper <- enquo(grouper);
  tmp <- groupedData %>% dplyr::filter(viewed & minutes_to_view>0) %>% 
    summarise(
      x:=max(!!grouper),
      std_dev=sd(minutes_to_view),
      y=fivenum(minutes_to_view)[3],
      size = n()
    ) %>% mutate(
      std_dev=ifelse(is.na(std_dev),0,std_dev),
      ySmooth=sgolayfilt(cumsum(y),p=3,n=25,m=1),
      std_devSmooth=sgolayfilt(cumsum(std_dev),p=3,n=25,m=1),
      sizeSmooth=sgolayfilt(cumsum(size),p=3,n=25,m=1)*25,
      lower95=predictLowCI(std_dev = std_devSmooth, sample_size = sizeSmooth, sample_median = ySmooth),
      upper95=predictHighCI(std_dev = std_devSmooth, sample_size = sizeSmooth, sample_median = ySmooth)
    )
  return(
    ggplot(tmp, aes(x=x))+
    geom_line(aes(y=ySmooth),stat="identity", colour="#0000ff",size=1)+
    geom_ribbon(
      aes(
        ymin=ifelse(lower95>0,lower95,0),
        ymax=ifelse(upper95<240,upper95,240)),
      fill="#8080ff",
      linetype="blank",
      alpha=0.4)+
    ylab("median ttv (mins)")+
    xlab(NULL)+
    scale_y_continuous(breaks=seq(0,6000,60))+#,limits=c(0,360))
    expand_limits(y=0)
  )
}

unviewedContinuous <- function(groupedData, grouper) {
  grouper <- enquo(grouper);
  tmp <- groupedData %>% 
    summarise(
      count=n(),
      x:=max(!!grouper),
      y=mean(ifelse(viewed,0,1))
    ) %>% mutate(
      countSmooth=sgolayfilt(cumsum(count),p=3,n=25,m=1),
      ySmooth=sgolayfilt(cumsum(y),p=3,n=25,m=1),
      std_error = ifelse(countSmooth > 0 & ySmooth>0, sqrt(ySmooth*(1-ySmooth)/countSmooth), NA)
    ) %>% dplyr::filter(!is.na(std_error))
  return(
  ggplot(
    tmp, aes(x=x))+
    geom_line(aes(y=100*ySmooth),stat="identity", colour="#ff0000",size=1)+
    geom_ribbon(
      aes(
        ymin=ifelse(100*(ySmooth-1.96*std_error)>0,100*(ySmooth-1.96*std_error),0),
        ymax=ifelse(100*(ySmooth+1.96*std_error)<15,100*(ySmooth+1.96*std_error),15)
      ),
      fill="#ff8080",
      linetype="blank",
      alpha=0.4)+
    ylab("results unviewed (%)")+
    xlab(NULL)+
    expand_limits(y=0)
  )
}

# by publish rate
maxX = max(data_baseline$publish_rate);
a<-ttvHeatmap(
  data_baseline %>% group_by(cuts = cut(publish_rate,breaks=seq(0,maxX,maxX/100))),
  publish_rate
  )+xlab("publication rate (per min)")
b<-ttvMedianContinuous(data_baseline %>% group_by(cuts = cut(publish_rate,breaks=seq(0,maxX,maxX/100))),publish_rate)+xlab("publication rate (per min)")
c<-unviewedContinuous(data_baseline %>% group_by(cuts = cut(publish_rate,breaks=seq(0,maxX,maxX/100))),publish_rate)+xlab("publication rate (per min)")

saveGraph(filename="publish_rate",vert_aspect=1.25,cols=1,plot=
            (a+rremove("xlab")+rremove("x.text")) + (b+rremove("xlab")+rremove("x.text")) + c + plot_layout(ncol = 1, heights = c(3,2,2))
)

# by minutes_processing
maxX = 24*60;
a<-ttvHeatmap(
  data_baseline %>% filter(minutes_processing < maxX & minutes_processing>0) %>% group_by(cuts = cut(minutes_processing,breaks=seq(0,maxX,maxX/100))),
  minutes_processing)+xlab("processing time")
b<-ttvMedianContinuous(
  data_baseline %>% filter(minutes_processing < maxX & minutes_processing>0) %>% group_by(cuts = cut(minutes_processing,breaks=seq(0,maxX,maxX/100))),
  minutes_processing)+xlab("processing time")
c<-unviewedContinuous(
  data_baseline %>% filter(minutes_processing < maxX & minutes_processing>0) %>% group_by(cuts = cut(minutes_processing,breaks=seq(0,maxX,maxX/100))),
  minutes_processing)+xlab("processing time")

saveGraph(filename="minutes_processing",vert_aspect=1.25,cols=1,plot=
            (a+rremove("xlab")+rremove("x.text")) + (b+rremove("xlab")+rremove("x.text")) + c + plot_layout(ncol = 1, heights = c(3,2,2))
)

# by age
a<-ttvHeatmap(data_baseline %>% group_by(patient_age),patient_age)
b<-ttvMedianContinuous(data_baseline %>% group_by(patient_age),patient_age)
c<-unviewedContinuous(data_baseline %>% group_by(patient_age),patient_age)+xlab("patient age (years)")

saveGraph(filename="patient_age",vert_aspect=1.25,cols=1,plot=
            (a+rremove("xlab")+rremove("x.text")) + (b+rremove("xlab")+rremove("x.text")) + c + plot_layout(ncol = 1, heights = c(3,2,2))
)

# weekday
a<-ttvHeatmap(data_baseline %>% filter(time_of_week<5*24*60) %>% group_by(cuts=cut(time_of_day,breaks=seq(0,24*60,10))),time_of_day)+
  scale_x_continuous(breaks=seq(0,24*60,60), labels=sprintf("%02d:00",seq(0,24,1)))+theme(axis.text.x = element_text(angle=90,size=9,hjust=1,vjust=0.5))
b<-ttvMedianContinuous(data_baseline %>% filter(time_of_week<5*24*60) %>% group_by(cuts=cut(time_of_day,breaks=seq(0,24*60,10))),time_of_day)+
  scale_x_continuous(breaks=seq(0,24*60,60), labels=sprintf("%02d:00",seq(0,24,1)))+theme(axis.text.x = element_text(angle=90,size=9,hjust=1,vjust=0.5))
c<-unviewedContinuous(data_baseline %>% filter(time_of_week<5*24*60) %>% group_by(cuts=cut(time_of_day,breaks=seq(0,24*60,10))),time_of_day)+
  scale_x_continuous(breaks=seq(0,24*60,60), labels=sprintf("%02d:00",seq(0,24,1)))+theme(axis.text.x = element_text(angle=90,size=9,hjust=1,vjust=0.5))

saveGraph(filename="weekday",vert_aspect=1.25,cols=1,plot=
            (a+rremove("xlab")+rremove("x.text")) + (b+rremove("xlab")+rremove("x.text")) + c + plot_layout(ncol = 1, heights = c(3,2,2))
)

# weekend
a<-ttvHeatmap(data_baseline %>% filter(time_of_week>5*24*60) %>% group_by(cuts=cut(time_of_day,breaks=seq(0,24*60,10))),time_of_day)+
  scale_x_continuous(breaks=seq(0,24*60,60), labels=sprintf("%02d:00",seq(0,24,1)))+theme(axis.text.x = element_text(angle=90,size=9,hjust=1,vjust=0.5))
b<-ttvMedianContinuous(data_baseline %>% filter(time_of_week>5*24*60) %>% group_by(cuts=cut(time_of_day,breaks=seq(0,24*60,10))),time_of_day)+
  scale_x_continuous(breaks=seq(0,24*60,60), labels=sprintf("%02d:00",seq(0,24,1)))+theme(axis.text.x = element_text(angle=90,size=9,hjust=1,vjust=0.5))
c<-unviewedContinuous(data_baseline %>% filter(time_of_week>5*24*60) %>% group_by(cuts=cut(time_of_day,breaks=seq(0,24*60,10))),time_of_day)+
  scale_x_continuous(breaks=seq(0,24*60,60), labels=sprintf("%02d:00",seq(0,24,1)))+theme(axis.text.x = element_text(angle=90,size=9,hjust=1,vjust=0.5))

saveGraph(filename="weekend",vert_aspect=1.25,cols=1,plot=
            (a+rremove("xlab")+rremove("x.text")) + (b+rremove("xlab")+rremove("x.text")) + c + plot_layout(ncol = 1, heights = c(3,2,2))
)

# Whole week
a<-ttvHeatmap(data_baseline %>% group_by(cuts=cut(time_of_week,breaks=seq(0,7*24*60,30))),time_of_week)+
  scale_x_continuous(breaks=seq(0,24*60*7,12*60), labels=c(paste0(rep(c("Mon ","Tue ","Wed ","Thu ","Fri ","Sat ","Sun "),each=2),rep(c("00:00","12:00"),7)),"Mon 00:00"))+theme(axis.text.x = element_text(angle=90,size=9,hjust=1,vjust=0.5))+
  geom_label(data=data.frame(
    x=c(12*60,18*60,(4*24+12)*60),
    y=c(2*60,12*60,(24*3)*60),
    label=c("A","B","C")
  ), aes(x=x,y=y,label=label))
b<-ttvMedianContinuous(data_baseline %>% group_by(cuts=cut(time_of_week,breaks=seq(0,7*24*60,30))),time_of_week)+
  scale_x_continuous(breaks=seq(0,24*60*7,12*60), labels=c(paste0(rep(c("Mon ","Tue ","Wed ","Thu ","Fri ","Sat ","Sun "),each=2),rep(c("00:00","12:00"),7)),"Mon 00:00"))+theme(axis.text.x = element_text(angle=90,size=9,hjust=1,vjust=0.5))
c<-unviewedContinuous(data_baseline %>% group_by(cuts=cut(time_of_week,breaks=seq(0,7*24*60,30))),time_of_week)+
  scale_x_continuous(breaks=seq(0,24*60*7,12*60), labels=c(paste0(rep(c("Mon ","Tue ","Wed ","Thu ","Fri ","Sat ","Sun "),each=2),rep(c("00:00","12:00"),7)),"Mon 00:00"))+theme(axis.text.x = element_text(angle=90,size=9,hjust=1,vjust=0.5))+
  geom_label(data=data.frame(
    x=c(30*60,(4*24+16)*60,(5*24+10)*60,(6*24+10)*60),
    y=c(3,9.5,9.1,8.7),
    label=c("D","E","F","F")
  ), aes(x=x,y=y,label=label))

saveGraph(filename="week",vert_aspect=1,cols=2,plot=
            (a+rremove("xlab")+rremove("x.text")) + (b+rremove("xlab")+rremove("x.text")) + c + plot_layout(ncol = 1, heights = c(3,2,2))
)

# delay from request to result??

################################
# ggplot(
#   data %>% 
#     mutate(emis_cat = cut(emis,breaks=c(-Inf,0.5,1.5,2.5,3.5,4.5,5.5,6.5,Inf), labels = c("0","1","2","3","4","5","6","7+"))) %>% 
#     group_by(viewed, emis_cat) %>% 
#     summarize(count=n()) %>% 
#     mutate(freq = count / sum(count)), 
#   aes(x=viewed, fill=viewed, y=emis_cat)
#   )+
#   geom_tile(aes(width=sqrt(freq)*0.95, height=sqrt(freq)*0.95), colour="black")+
#   coord_flip()
# 
# ggplot(
#   data %>% group_by(viewed, nhsnos) %>% summarize(count=n()) %>% mutate(freq = count / sum(count)), 
#   aes(x=viewed, fill=viewed, y=nhsnos)
# )+
#   geom_tile(aes(width=freq, height=0.9), colour="black")+
#   coord_flip()
# 
# ggplot(
#   data %>% group_by(viewed,patient_id_updated) %>% summarize(count=n()) %>% mutate(freq = count / sum(count)), 
#   aes(x=viewed, y=patient_id_updated, fill=viewed)
# )+
#   geom_tile(aes(width=sqrt(freq)*0.95, height=sqrt(freq)*0.95), colour="black")+
#   coord_flip()
# ggsave('viewedVersusIdentifierUpdates.png',width=5,height=5,units='in')

# +scale_y_discrete(limits=c(0,15))
#  geom_boxplot(width=0.1, fill="grey")
#+scale_y_continuous(limits=c(0,7))


# 
# pm <- ggpairs(data,
#               columns=c("minutes_to_view", "dependency_level", "emis", "nhsnos", "mrns", "patient_id_updated"),
#               mapping=aes(colour=viewed)
#               )



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


# # minutes processing vs time to view
# ggplot(data,aes(x=minutes_processing,y=minutes_to_view))+geom_bin2d(bins=100)+
#   scale_x_continuous(limits=c(0,600))+
#   scale_y_continuous(limits = c(0,600))
# 
# ggplot(data,aes(x=minutes_processing,y=minutes_to_view))+geom_bin2d(bins=100)+
#   scale_x_log10()+
#   scale_y_log10()

# ttv distribution by hour

data_count <- nrow(data_baseline);
viewed_total <- nrow(data_baseline %>% filter(viewed))

data_by_hour_to_view <- data_baseline %>%
  filter(minutes_to_view > 0 & viewed) %>%
  group_by(minutes_to_view) %>%
  summarize(
    count=n(),
    viewed=sum(ifelse(viewed,1,0))
  ) %>% mutate(
    cumulative=cumsum(count),
    cum_views=cumsum(viewed),
    viewed_percent=cum_views/data_count,
    density=sgolayfilt(count,n=25,p=3)/viewed_total
  )

ttv_distribution <- 
ggplot(data_by_hour_to_view, aes(x=minutes_to_view)) +
  geom_hline(yintercept = viewed_total/data_count/100, colour="#ff8080") +
  geom_area(aes(y=density),fill="#808080",linetype=0)+
  geom_line(aes(y=density),colour="#000000",size=1)+
  scale_x_continuous(limits=c(0,12*60),breaks=seq(0,12*60,3*60),labels=seq(0,12,3))+xlab("hours since result")+
  geom_line(aes(y=viewed_percent/100),colour="red",size=1)+
  scale_y_continuous(limits=c(0,0.01),sec.axis=sec_axis(~.*10000, name="percent viewed")
  )
# ggsave('timeToViewDistribution12Hours.svg',width=89,height=5,units='mm')

# base_theme <- theme_get()
# theme_replace(axis.text=element_text(size=6))

ttv_distribution_log <- 
ggplot(data_by_hour_to_view, aes(x=minutes_to_view)) +
  geom_hline(yintercept = viewed_total/data_count/100, colour="#ff8080") +
  geom_area(aes(y=density),fill="#808080",linetype=0)+
  geom_line(aes(y=density),colour="#000000",size=1)+
  scale_x_log10(breaks=c(1,60,(6*60),(24*60),(7*24*60),(28*24*60)),labels=c("1m","1h","6h","1d","1w","4w"))+xlab("time since result")+
  geom_line(aes(y=viewed_percent/100),colour="red",size=1)+
  scale_y_continuous(limits=c(0,0.01),sec.axis=sec_axis(~.*10000, name="percent viewed")
)


a <-  ttv_distribution+ggtitle("A")+
  # scale_y_continuous(limits=c(0,0.01),sec.axis=sec_axis(~.*10000, labels = NULL,name=NULL))+
  theme(
    axis.text.y.right = element_blank(), 
    axis.title.y.right = element_blank(),
    plot.title = element_text(hjust = -0.1),
    # axis.line.y.right=element_blank(),
    # axis.ticks.y.right=element_blank(),
    plot.margin = unit(c(0.25, 0.25, 0.25, 0.25), "cm")
  )
b <-  ttv_distribution_log+ggtitle("B")+
  scale_y_continuous(limits=c(0,0.01),labels=NULL,name=NULL,sec.axis=sec_axis(~.*10000,name="percent viewed"))+
  theme(
    #axis.text.y = element_blank(), 
    #axis.title.y = element_blank(),
    #axis.line.y=element_blank(),
    #axis.ticks.y=element_blank(),
    plot.title = element_text(hjust = -0.1),
    plot.margin = unit(c(0.25, 0.25, 0.25, 0.25), "cm")
  )

saveGraph(filename="distributions", vert_aspect=0.5,plot=a+b)
# For guidance, Nature's standard figure sizes are 89 mm wide (single column) and 183 mm wide (double column). 
# The full depth of a Nature page is 247 mm. Figures can also be a column-and-a-half where necessary (120–136 mm).

# clinician_views_distribution <- ggplot(data, aes(x=total_views)) +
# geom_histogram(bins = max(data$total_views),fill="#ffb080",colour="black") +
# xlab("no. clinicians")+
# scale_x_continuous(breaks=seq(0,50,5))
# clinician_views_distribution
# ggsave('clinicianViewsDistribution.png',width=10,height=5,units='in')
# 
# distribution <- plot_grid(
#   ttv_distribution_1_day,
#   clinician_views_distribution,
#   nrow=1,
#   align="v",
#   rel_widths=c(2,1),
#   labels = c("A","B")
# )
# distribution
# 
# save_plot("distributionPub.png", distribution,base_height = 3,base_width = 10)

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