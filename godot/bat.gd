extends CharacterBody2D

signal first_flap
signal any_flap
signal hit_pipe_or_water

const SPEED = 300.0
const JUMP_VELOCITY = -400.0
var is_first_flap = true
var is_game_over = false

func _ready() -> void:
	$AnimatedSprite2D.play("idle")

func _input(event: InputEvent) -> void:
	# Handle screen tap for mobile/web
	if event is InputEventScreenTouch and event.pressed:
		_flap()


func _physics_process(delta: float) -> void:
	# Always apply gravity so the bat falls.
	velocity += get_gravity() * delta
	if is_game_over:
		velocity.y += 400 ## fall faster weh dying 

	# Handle jump/fly - bat can fly up anytime spacebar is pressed.
	if Input.is_action_just_pressed("ui_accept"):
		_flap()

	# Slow down animation when falling
	if velocity.y > 0:
		$AnimatedSprite2D.speed_scale = 0.5

	move_and_slide()


func _flap() -> void:
	any_flap.emit()
	if is_game_over:
		return
	if is_first_flap:
		first_flap.emit()
		is_first_flap = false
	$AnimatedSprite2D.speed_scale = 2.0
	$AnimatedSprite2D.play('fly')
	
	velocity.y = JUMP_VELOCITY

func reset_for_new_game():
	is_first_flap = true
	is_game_over = false
	$AnimatedSprite2D.play("idle")

func _on_pipe_detector_area_entered(area: Area2D) -> void:
	hit_pipe_or_water.emit()
	pass
	
func game_over():
	is_game_over = true
	$AnimatedSprite2D.play('dead')
