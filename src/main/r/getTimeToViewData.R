library(DBI)
library(odbc)
library(datasets)
library(dplyr)
library(reshape2)

pwd <- readline(prompt="Enter DB: ")
con <- dbConnect(odbc(),
                 Driver = "ODBC Driver 13 for SQL Server",
                 Server = "10.174.129.118",
                 Database = "RobsDatabase",
                 UID = "RobertSQL",
                 PWD = pwd,
                 Port = 1433);

dbListTables(con);

data <- dbReadTable(con, "aggTimeToView");

data <- data %>%
	mutate(
			minutes_to_view = ifelse(minutes_to_view > 31*24*60, NA, minutes_to_view),
			first_viewed_date = ifelse(is.na(minutes_to_view), NA, first_viewed_date),
			view_type = ifelse(is.na(minutes_to_view), NA, view_type),
			total_views = ifelse(is.na(minutes_to_view), 0, total_views),
			viewed = !is.na(minutes_to_view)
)

data <- data %>%
  mutate(month_cat=format(date, "%Y-%m")) %>%
  mutate(time_of_week=as.numeric(difftime(date,as.POSIXct(cut(date,"week")),unit="mins"))%%(24*7*60)) %>%
  mutate(time_of_day=time_of_week %% (24*60)) %>%
  filter(as.Date(date) <= as.Date("2017-08-31")) %>%
  collect();

viewed_data <- data %>% 
  filter(total_views > 0) %>%
  mutate(time_of_week_viewed=as.numeric(difftime(first_viewed_date,as.POSIXct(cut(first_viewed_date,"week")),unit="mins"))%%(24*7*60)) %>%
  mutate(time_of_day_viewed=time_of_week_viewed %% (24*60)) %>%
  mutate(end_month_cat=format(first_viewed_date, "%Y-%m")) %>%
  collect();

data_haem <- data %>% 
  filter(discipline_name=='Chem/Haem') %>%
  collect();

viewed_data_haem <- viewed_data %>% 
  filter(discipline_name=='Chem/Haem') %>%
  collect();

data_baseline <- filter(data, (
  investigation_abnormal==1 
  & discipline_name=='Chem/Haem')
  & (is.na(minutes_to_view) | minutes_to_view < 30*24*60) # Needs rethinking
  & (is.na(first_viewed_date) | as.Date(first_viewed_date) < as.Date("2017-10-01"))
  & as.Date(date) >= as.Date("2014-09-01")
);

viewed_data_baseline <- filter(viewed_data, 
  investigation_abnormal==1 
  & discipline_name=='Chem/Haem'
  & minutes_to_view < 30*24*60 # Needs rethinking
  & as.Date(first_viewed_date) < as.Date("2017-10-01")
  & as.Date(date) >= as.Date("2014-09-01")
);


# cor(data$patient_age, data$minutes_to_view, use="complete.obs")
# cor(data$date, data$minutes_to_view, use="complete.obs")
# https://stats.stackexchange.com/questions/119835/correlation-between-a-nominal-iv-and-a-continuous-dv-variable/124618#124618
# model.lm <- lm(minutes_to_view ~ month_cat, data = data)
