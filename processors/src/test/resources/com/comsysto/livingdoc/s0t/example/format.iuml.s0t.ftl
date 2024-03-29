<#ftl attributes={"s0t:is-template":true}/>
skinparam class {
	BackgroundColor #F0F0F0/#C0C0C0
	BorderColor grey
    BackgroundColor<<new>> #e6ffe6/#b3ffb3
    BorderColor<<new>> green
    BorderColor<<new_in_context>> green
	BorderColor<<modified>> blue
	BackgroundColor<<modified>> #e6e6ff/#b3b3ff
	BackgroundColor<<required>> #ffd700/yellow
    BorderColor<<required>> orange
	ArrowColor black
}

skinParam noteBackgroundColor #FFFFAA
skinParam noteBorderColor gray

skinparam sequence {
	BoxBackgroundColor #FAFAFA
	BoxBorderColor grey
	ArrowColor black
	ActorBorderColor grey
	LifeLineBorderColor black
	LifeLineBackgroundColor #E0E0E0|#FFFFFF
	
	ParticipantBorderColor grey
	ParticipantBackgroundColor #F0F0F0/#C0C0C0
	ParticipantBorderColor<<new>> green
	ParticipantBackgroundColor<<new>> #e6ffe6/#b3ffb3
	ParticipantBorderColor<<new_in_context>> green
	ParticipantBorderColor<<modified>> blue
	ParticipantBackgroundColor<<modified>> #e6e6ff/#b3b3ff
	ParticipantBackgroundColor<<required>> #ffd700/yellow
	ParticipantBorderColor<<required>> orange
	ParticipantFontColor black
	
	ActorBackgroundColor #F0F0F0/#C0C0C0
	ActorBackgroundColor<<new>> #e6ffe6/#b3ffb3
}
