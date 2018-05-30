
log4j.propreties /Users/h/Downloads/atlassian-jira-software-7.4.2-standalone/atlassian-jira/WEB-INF/classes/log4j.properties

#####################################################
# ISF appender
#####################################################

log4j.appender.isflog=com.atlassian.jira.logging.JiraHomeAppender
log4j.appender.isflog.File=jira-isf.log
log4j.appender.isflog.MaxFileSize=20480KB
log4j.appender.isflog.MaxBackupIndex=5
log4j.appender.isflog.layout=com.atlassian.logging.log4j.NewLineIndentingFilteringPatternLayout
log4j.appender.isflog.layout.ConversionPattern=%d %t %X{jira.username} %X{jira.request.id} %X{jira.request.assession.id} %X{jira.request.url} %m%n


#####################################################
# ISF Logger
#####################################################

log4j.logger.org.jasig.cas.client.integration.atlassian = ERROR, isflog
