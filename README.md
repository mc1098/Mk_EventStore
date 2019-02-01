# Mk_EventStore

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Build Status](https://travis-ci.com/mc1098/Mk_EventStore.svg?branch=master)](https://travis-ci.com/mc1098/Mk_EventStore)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/mc1098/Mk_EventStore.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mc1098/Mk_EventStore/context:java)


A simple EventStore project for saving Entity's using event sourcing. Provides a custom method for saving Events and Snapshots in an effort to reduce the amount of IO to a minimum. 
Transactional and atomic updating of events. 

Version 0.5
The basic Mk_ implementations work for local usage of the EventStore but many improvements are needed to be a complete and dependable
storage option. 
