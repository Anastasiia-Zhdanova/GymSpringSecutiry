INSERT INTO training_type (training_type_name) VALUES ('Potions') ON CONFLICT (training_type_name) DO NOTHING;
INSERT INTO training_type (training_type_name) VALUES ('Charms') ON CONFLICT (training_type_name) DO NOTHING;
INSERT INTO training_type (training_type_name) VALUES ('Transfiguration') ON CONFLICT (training_type_name) DO NOTHING;
INSERT INTO training_type (training_type_name) VALUES ('Herbology') ON CONFLICT (training_type_name) DO NOTHING;
INSERT INTO training_type (training_type_name) VALUES ('Duelling') ON CONFLICT (training_type_name) DO NOTHING;
INSERT INTO training_type (training_type_name) VALUES ('Flying') ON CONFLICT (training_type_name) DO NOTHING;