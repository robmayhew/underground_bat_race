extends Node2D


var start_bat_position 
var start_water_position
var pipe_scene:PackedScene
var pipe_positions = [-800]
var score = 0
var is_animating_score = false
var is_game_over = false 

var rng = RandomNumberGenerator.new()


func _ready() -> void:
	start_bat_position = $Bat.position
	start_water_position = $Waterline.position
	pipe_scene = ResourceLoader.load("res://pipe.tscn")
	add_pipes()
	pass 

func _process(delta: float) -> void:
	check_if_need_more_pipes()
	pass


	
	


func add_pipes() -> void:
	var y = pipe_positions[-1]
	for i in range(4):
		y = y + 100 + randf_range(300,400) + (100 * randi_range(0,4))
		pipe_positions.append(y)
		var pipe = pipe_scene.instantiate()
		pipe.position.y = -1 * y;
		pipe.position.x = 100 - randi_range(1,200)
		pipe.speed = randi_range(90,140)
		add_child(pipe)
		


func _on_bat_bat_scored() -> void:
	score = score + 1
	$UI/Score.text = str(score)
	animate_score_pop()
	pass 


func _on_bat_hit_pipe_or_water() -> void:
	if is_game_over:
		return
	is_game_over = true
	Globals.game_over(score)
	$Bat.game_over()
	$GameOverTimer.start()
	pass 
	
func check_if_need_more_pipes() -> void:
	var lastPipe = pipe_positions[-1]
	var diff = lastPipe - $Bat.position.y
	if diff > - 500:
		add_pipes()

func animate_score_pop() -> void:
	if is_animating_score:
		return
	var score_label = $UI/Score
	is_animating_score = true

	# Create a tween for smooth animations
	var tween = create_tween()
	tween.set_parallel(true)

	# Scale animation: pop bigger then back to normal
	tween.tween_property(score_label, "scale", Vector2(1.5, 1.5), 0.1)
	tween.chain().tween_property(score_label, "scale", Vector2(1.0, 1.0), 0.2).set_ease(Tween.EASE_OUT).set_trans(Tween.TRANS_BACK)

	# Color cycling animation through vibrant colors
	var colors = [
		Color(1.0, 0.2, 0.2),  # Red
		Color(1.0, 0.5, 0.0),  # Orange
		Color(1.0, 0.9, 0.0),  # Yellow
		Color(0.2, 1.0, 0.2),  # Green
		Color(0.0, 0.8, 1.0),  # Cyan
		Color(0.6, 0.2, 1.0),  # Purple
	]

	# Pick a random color to cycle through
	var color1 = colors[randi() % colors.size()]
	var color2 = colors[randi() % colors.size()]

	tween.tween_property(score_label, "modulate", color1, 0.1)
	tween.chain().tween_property(score_label, "modulate", color2, 0.1)
	tween.chain().tween_property(score_label, "modulate", Color.WHITE, 0.1)

	# Reset animation flag when done
	tween.finished.connect(func(): is_animating_score = false)


func _on_game_over_timer_timeout() -> void:
	SceneManager.change_scene("res://title.tscn");
	pass # Replace with function body.


func _on_server_server_ready() -> void:
	print("Server ready. Seed is " + str(Globals.ranom_seed))
	rng.seed = Globals.ranom_seed

	pass # Replace with function body.
