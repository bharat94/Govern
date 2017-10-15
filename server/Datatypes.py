class User():
	def __init__(self, name, phone, google_id, governing_locations):
		self.name = name
		self.phone = phone
		self.google_id = google_id
		self.governing_locations = governing_locations

class GovernLocation():
	def __init__(self, latitude, longitude, area_name, issues):
		self.latitude = latitude
		self.longitude = longitude
		self.area_name = area_name
		self.issues = issues

class Issue():
	def __init__(self, issue_name, photo):
		self.issue_name = issue_name
		self.photo = photo