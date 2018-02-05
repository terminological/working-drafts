

plot_weekday_xaxis <- function(plot) {
  return(plot +
           geom_vline(xintercept = 0, colour='grey')+
           geom_vline(xintercept = 24, colour='grey')+
           geom_vline(xintercept = 48, colour='grey')+
           geom_vline(xintercept = 72, colour='grey')+
           geom_vline(xintercept = 96, colour='grey')+
           geom_vline(xintercept = 120, colour='grey')+
           geom_vline(xintercept = 144, colour='grey')+
           geom_vline(xintercept = 168, colour='grey')+
           xlab("hours from midnight sunday") +
           scale_x_continuous(breaks = seq(0, 24*7, 6))
  );
}

plot_day_xaxis <- function(plot) {
  return(plot +
    xlab("hours from midnight") +
    scale_x_continuous(breaks = seq(0, 24, 1))
  );
}

plot_year_xaxis <- function(plot) {
  return(plot +
           xlab("month of year")+
           scale_x_discrete(breaks = 
                c('01-01','02-01','03-01','04-01','05-01','06-01','07-01','08-01','09-01','10-01','11-01','12-01'),
                   labels = c('Jan','Feb','Mar','Apr','May','June','July','Aug','Sep','Oct','Nov','Dec'))
  );
}

plot_year_xaxis_2 <- function(plot) {
  return(plot +
           xlab("month of year")+
           scale_x_discrete(breaks = 
              c('01','02','03','04','05','06','07','08','09','10','11','12'),
                            labels = c('Jan','Feb','Mar','Apr','May','June','July','Aug','Sep','Oct','Nov','Dec'))
  );
}