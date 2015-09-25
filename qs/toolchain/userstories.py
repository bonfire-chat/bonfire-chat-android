#!/usr/bin/env python2
# -*- coding: utf-8 -*-

import MySQLdb as mdb
import sys
from array import *

times = array('f', [1738.0/60, 2768.0/60, 990.0/60, 4825.0/60, 4650.2/60, 4062.0/60, 3834.2/60, 5289.0/60, 1316.0/60, 0])

def myFormat(s):
    if s is not None:
        return '\parbox[t]{10cm}{'+ s.replace('\n', '\\\\').replace('%', '\\%').replace(' \"', ' ``').replace('\" ', '\'\' ').replace('\".', '\'\'.').replace('\"-', '\'\'-').replace('"', '').replace('\"', '').replace('„', '').replace('“', '\'\'') + '}'
    else:
        return " "

con = mdb.connect('37.59.184.73', 'qsmakefile', '2C6rJdvkgS', 'openproject_ce')

with con:

    cur = con.cursor(mdb.cursors.DictCursor)

    cur.execute("select w.id, subject, description, created_at, story_points, c.value as acceptance_criteria, w.fixed_version_id as sprint, u.firstname, u.lastname, (select sum(t.hours) from time_entries t where t.work_package_id=w.id and t.project_id=1) as hours from work_packages w left outer join custom_values c on w.id=c.customized_id and c.custom_field_id=3  left outer join users u on u.id=w.assigned_to_id where w.type_id=2 and w.project_id=1 order by sprint, created_at asc")

    rows = cur.fetchall()

    lastSprint = 0
    sprintHours = 0
    for row in rows:

        if row['story_points'] is not None:
            sp = row['story_points']
        else:
            sp = 0;

        if row['hours'] is not None:
            hours = row['hours']
        else:
            hours = 0

        if sp != 0:
            velocity = hours/sp
        else:
            velocity = 0

        if sp == 0 or hours == 0 or velocity == 0:
            sys.stderr.write('Userstory ' + str(row["id"]) + ' muss noch bearbeitet werden\n')

        sprint = row["sprint"]
        sprintHours += hours

        if sprint > lastSprint:
            lastSprint = sprint
            print '\\section*{Sprint ' + str(sprint) + '}'
            sprintHours -= hours
            if sprint > 1:
                missingHours = times[sprint - 2] - sprintHours
                sys.stderr.write('In Sprint ' + str(sprint-1) + ' fehlen noch ' + str("{:.2f}".format(missingHours)) + ' Stunden\n')
            sprintHours = hours


        print '\\centering'
        print '\\begin{tabular}{| p{5cm} | p{10cm} |}'
        print '\\hline'
        print 'ID & ', row["id"]
        print '\\tabularnewline'
        print '\\hline'
        print 'Name & ', myFormat(row['subject'])
        print '\\tabularnewline'
        print '\\hline'
        print 'Beschreibung & ', myFormat(row['description'])
        print '\\tabularnewline'
        print '\\hline'
        print 'Akzeptanzkriterium & ', myFormat(row['acceptance_criteria'])
        print '\\tabularnewline'
        print '\\hline'
        print 'Story Points & ', sp
        print '\\tabularnewline'
        print '\\hline'
        print 'Entwickler & ', row['firstname'], ' ', row['lastname']
        print '\\tabularnewline'
        print '\\hline'
        print 'Umgesetzt Iteration & ', sprint
        print '\\tabularnewline'
        print '\\hline'
        print 'Tatsächlicher Aufwand (Std.) & ', "{:.2f}".format(hours)
        print '\\tabularnewline'
        print '\\hline'
        print 'Velocity (Std./Story Point) & ', "{:.2f}".format(velocity)
        print '\\tabularnewline'
        print '\\hline'
        print '\\end{tabular}'
        print '\\\\'
        #print '\\vspace{1cm}'

con.close()
