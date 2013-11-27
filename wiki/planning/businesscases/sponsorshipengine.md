# Sponsorship Engine

Author: Simon Pamies

## Rationale
SAP is getting involved in more and more sponsorship agreements in the sports sector and for each of these pushes to support the partner with technology. SAP puts a strong focus on providing analytics that not only help people understand the sport and making it visible, but also thrives to help partners run their sport better. 

These analytics, in most cases, rely on external data providers that feed data into a system SAP develops and maintains. Over time or even at once, different data providers may need to be used. The data sources need to be adapted to fit a reasonable model of the domain based on which they can be enriched with analytics specific to the sport and output either to a database or directly fed into a user interface.

In most cases, there is an important distinction between live and non-live mode. During an event data is flowing into the SAP analytics and needs to be processed in realtime. In non-live mode data processing is not time-critical. As these partnerships aim to provide a live experience the main focus of all projects is to analyze data in realtime but to also provide historical data.

## Purpose

Provide developers, aiming to implement an analytical solution that analyzes data in live and non-live mode, a framework and architecture that supports their use case. Make it possible to share knowledge and resources by sharing code across projects.

## Features

* Flexible Management of Code-Parts (OSGi)
* Fast and flexible embedded Web-Server
* Builtin life-cycle support (build, test, release and deploy)
* Scaling
    * Replication (horizontal)
    * EC2 Integration (vertical)
* Best-Practices
    * User Interfaces with GWT
    * Providing a public API
    * Connectors to external data providers
    * Unit- and Integration-Tests
    * In-Memory Analytics and Caching
    * Proven concurrency management utilizing multi-core architectures efficiently
    * Space efficient data structures
    * Eclipse Integration

## Description
All the features mentioned above are available in bundles that are part of the Sailing Analytics project. It works very well for all needs related to this specific sport and the data providers known there. The software is grouped into so called bundles that are, in theory, independent from each other and provide a certain part of the logic (e.g. replication).

Looking deeper into the current stack, most of the bundles contain code specific for the domain of sailing and in addition to that are strongly dependent from each other. The major work and cost driver of this project will be to extract and rewrite components that have been identified to become part of the shared code base.

Once such a shared code base is in place and documented, other developers could easily build on it and focus on implementing code related to their domain instead of having to re-invent things that already exist. They would then also be able to get support from other developers working with the same framework. Particularly tricky aspects such as concurrency management with locking and caching then will not have to be solved several times.

Such a project could also be described as a first step towards a productization process as the outcome is, in terms of documentation and cleanliness of code and architecture, the foundation of something that can be shared publicly.

Cost wise this project needs a big upfront investment but will pay off itself after a short time because other projects can benefit from everything that has been developed until then but will also benefit from future development.

## Synergies

This project will leverage many synergies at least between all developers working on this type of projects.

## Risks

There are no risks that could make this project fail, as the steps and the implications are known. The biggest risk is to underestimate the time needed to extract all the components and to provide users with a good documentation. But this risk is mitigated by the fact that other projects can make use of it at an early stage even if not everything is yet extracted and documented.

Another risk is a potential slow-down of the activities around the Sailing Analytics because such a project only makes sense if the Sailing Analytics leverage the extracted code base as well.

## Prototype

There is currently no prototype.

## Estimation

It will take 2-4 weeks to come to a point where other sponsorship projects could start being built on top of it. It will take 3-6 weeks on top of the initial work amount to come to a version that is well documented and can be shared with developers that have no knowledge about the underlying details.