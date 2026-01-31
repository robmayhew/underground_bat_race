extends Control

# Delay to avoid game taps from restating a new game too soon
var ignore_start = true

# Called when the node enters the scene tree for the first time.
func _ready() -> void:
	pass 


# Called every frame. 'delta' is the elapsed time since the previous frame.
func _process(delta: float) -> void:
	pass


func _on_button_pressed() -> void:
	ignore_start = false
	start_game()
	pass

# Handle input for spacebar and tap/click
func _input(event: InputEvent) -> void:
	# Check for spacebar press
	if event is InputEventKey:
		if event.pressed and event.keycode == KEY_SPACE:
			start_game()

	# Check for mouse click or touch tap
	if event is InputEventMouseButton:
		if event.pressed and event.button_index == MOUSE_BUTTON_LEFT:
			start_game()

	if event is InputEventScreenTouch:
		if event.pressed:
			start_game()
			

func start_game() -> void:
	if ignore_start:
		return
	SceneManager.change_scene("res://game.tscn");	

func _on_wait_to_start_timer_timeout() -> void:
	ignore_start = false
	pass
