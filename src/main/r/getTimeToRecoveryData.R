library(DBI)
library(odbc)
library(datasets)
library(dplyr)
library(reshape2)
library(hexbin)

source_directory <- getSrcDirectory(function(dummy) {dummy});
if (source_directory == "") source_directory="/home/robchallen/Git/working-drafts/src/main/r"
source(paste(source_directory,'/standardAxes.R',sep=""));
source(paste(source_directory,'/utils.R',sep=""));
createDirectory();

pwd <- readline(prompt="Enter DB: ");
# pwd <- rstudioapi::askForPassword("Database password");

con <- dbConnect(odbc(),
                 Driver = "ODBC Driver 13 for SQL Server",
                 Server = "10.174.129.118",
                 Database = "RobsDatabase",
                 UID = "RobertSQL",
                 PWD = pwd,
                 Port = 1433);

dbListTables(con);

data2 <- dbReadTable(con, "aggTimeToRecovery");
data2_count <- nrow(data2);
data2_by_hour_to_resolution <- data2 %>%
		mutate(
				hours_to_resolution = minutes_to_resolution %/% 60,
				total = n()
		) %>%
		group_by(hours_to_resolution) %>%
		summarize(
				count=n()
		) %>% mutate(
				cumulative=cumsum(count),
				recovered_percent=cumulative / data2_count
		)

ggplot(data = data2_by_hour_to_resolution %>% filter(hours_to_resolution < 240),
		aes(x=hours_to_resolution,y=count))+
		geom_bar(stat="identity")+
		geom_line(aes(y=recovered_percent*15000))+
		xlab("Hours from abnormality")+
		ylab("normal test results");

data_creat <- data2 %>% filter(test=="CREA")

cor(x=data_creat$minutes_to_view, y=data_creat$minutes_to_resolution, use="complete.obs")
cor(x=data2$patient_age, y=data2$minutes_to_resolution, use="complete.obs")
cor(x=data2$patient_age, y=data2$minutes_to_resolution, use="complete.obs")

summary(lm(minutes_to_resolution ~ minutes_to_view, data=data2))

cor(data2$minutes_to_resolution, data2$minutes_to_view, use="complete.obs")

timeToRecoveryByTimeToView <- ggplot(
  data=data2 %>% filter(minutes_to_view > 0) %>% filter(minutes_to_view < 4*60) %>% filter( minutes_to_resolution < 5*24*60), 
  aes(x=minutes_to_view, y=minutes_to_resolution)) +
  geom_bin2d(na.rm = TRUE)

timeToRecoveryByTimeToView
ggsave('timeToRecoveryByTimeToView.png',width=10,height=5,units='in')

# cor(data_no_na$patient_age, data_no_na$minutes_to_resolution)
# cor(data2, use="complete.obs", method="kendall") 

# https://stats.stackexchange.com/questions/108007/correlations-with-categorical-variables


# ggplot(data2,aes(x=minutes_to_resolution,y=minutes_to_view)) + 
#   geom_point(colour="blue", alpha=0.2) + 
#   geom_density2d(colour="black")