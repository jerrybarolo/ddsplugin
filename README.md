ddsplugin
=========

*In this example we used RTI DDS 5.1.0* (http://www.rti.com/downloads/rti-dds-files.html)

To install RTI DDS into the local repository use the following steps

    1) mvn install:install-file -Dfile=nddsjava.jar -DgroupId=com.rti -DartifactId=ndds -Dpackaging=jar -Dversion=5.1.0
    2) mvn install:install-file -Dfile=nddsjavad.jar -DgroupId=com.rti -DartifactId=nddsd -Dpackaging=jar -Dversion=5.1.0

Finally add the dependencies to your pom.xml:

    <dependency>
        <groupId>com.rti</groupId>
        <artifactId>ndds</artifactId>
        <version>5.1.0</version>
    </dependency>
    <dependency>
        <groupId>com.rti</groupId>
        <artifactId>nddsd</artifactId>
        <version>5.1.0</version>
    </dependency>
    
For compile the IDL write:

"%NDDSHOME%\scripts\rtiddsgen.bat" -package com.next.idlcode -d . -language Java -ppDisable -replace ..\..\TSHA.idl

in src.main.java directory