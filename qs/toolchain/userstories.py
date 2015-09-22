#!/usr/bin/python2
# -*- coding: utf-8 -*-

import MySQLdb as mdb

def myFormat(s):
    if s is not None:
        return '\parbox[t]{10cm}{'+ s.replace('\n', '\\\\').replace('%', '\\%').replace('"', '').replace('\"', '').replace('„', '').replace('“', '') + '}'
    else:
        return " "

con = mdb.connect('37.59.184.73', 'qsmakefile', '2C6rJdvkgS', 'openproject_ce')

with con:

    cur = con.cursor(mdb.cursors.DictCursor)

    cur.execute("select w.id, subject, description, created_at, story_points, c.value as acceptance_criteria, w.fixed_version_id as sprint, u.firstname, u.lastname, (select sum(t.hours) from time_entries t where t.work_package_id=w.id) as hours from work_packages w left outer join custom_values c on w.id=c.customized_id and c.custom_field_id=3  left outer join users u on u.id=w.assigned_to_id where w.type_id=2 and project_id=1 order by sprint, created_at asc")

    rows = cur.fetchall()

    print '\\begin{center}'

    for row in rows:
        if row['story_points'] is not None:
            sp = row['story_points']
            if row['hours'] is not None and sp != 0:
                velocity = row['hours']/sp
                hours = row['hours']*1
            else: 
                velocity = 0
                hours = 0
        else:
            velocity = 0
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
        print 'Story Points & ', "{:.2f}".format(sp)
        print '\\tabularnewline'
        print '\\hline'
        print 'Entwickler & ', row['firstname'], ' ', row['lastname']
        print '\\tabularnewline'
        print '\\hline'
        print 'Umgesetzt Iteration & ', row['sprint']
        print '\\tabularnewline'
        print '\\hline'
        print 'Tatsächlicher Aufwand (Std.) & ', "{:.2f}".format(hours)
        print '\\tabularnewline'
        print '\\hline'
        print 'Velocity (Std./Story Point) & ', "{:.2f}".format(velocity)
        print '\\tabularnewline'
        print '\\hline'
        print '\\end{tabular}'
        print '\\vspace{1cm}'

    print '\\end{center}'

con.close()
