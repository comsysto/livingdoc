<#ftl attributes={"s0t:is-template":true}/>
<#-- @ftlvariable name="s0tModel" type="com.comsysto.livingdoc.s0t.model.S0tModel" -->
@startuml
!include ./format.iuml
title Example sequence diagram

<@S0tSequences.renderSequences s0tModel/>

@enduml
