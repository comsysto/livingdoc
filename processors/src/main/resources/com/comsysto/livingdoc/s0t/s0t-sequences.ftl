<#macro renderSequences s0tModel>
<#-- @ftlvariable name="s0tModel" type="com.comsysto.livingdoc.s0t.model.S0tModel" -->
    <#list s0tModel.executables?keys as executableName>
        participant ${executableName.typeName.simpleName}
    </#list>

    <#list s0tModel.executables?values as source>
        activate ${source.name.typeName.simpleName}
        <@renderSequence source/>
        deactivate ${source.name.typeName.simpleName}
    </#list>
</#macro>

<#macro renderSequence source>
<#-- @ftlvariable name="source" type="com.comsysto.livingdoc.s0t.model.ExecutableModel" -->
    <#list source.outgoingCalls as target>
        ${source.name.typeName.simpleName} -> ${target.name.typeName.simpleName}<#if target.signature??>: ${target.signature}</#if>
        <#if target.outgoingCalls?has_content>
            activate ${target.name.typeName.simpleName}
            <@renderSequence target/>
            deactivate ${target.name.typeName.simpleName}
        </#if>
    </#list>
</#macro>
