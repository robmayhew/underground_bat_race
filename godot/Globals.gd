extends Node

var times_played = 0
var last_score = 0
var best_score = 0
var just_got_a_high_score = false
var ranom_seed = 0

func game_over(score):
	times_played = times_played + 1
	last_score = score
	just_got_a_high_score = score > best_score
	if just_got_a_high_score:
		best_score = last_score


# Called when the node enters the scene tree for the first time.
func _ready() -> void:
	pass # Replace with function body.


# Called every frame. 'delta' is the elapsed time since the previous frame.
func _process(delta: float) -> void:
	pass
