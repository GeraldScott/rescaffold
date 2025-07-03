INSERT INTO person (first_name, last_name, email, gender_id, title_id) VALUES
    ('Thabo', 'Pityana', 'thabo.pityana@example.co.za', (SELECT id FROM gender WHERE code = 'M'), (SELECT id FROM title WHERE code = 'MR')),
    ('Nkosazana', 'Olifant', 'nkosazana.olifant@example.co.za', (SELECT id FROM gender WHERE code = 'F'), (SELECT id FROM title WHERE code = 'DR')),
    ('Sipho', 'Nkuna', 'sipho.nkuna@example.co.za', (SELECT id FROM gender WHERE code = 'M'), (SELECT id FROM title WHERE code = 'MR')),
    ('Lerato', 'Mokoena', 'lerato.mokoena@example.co.za', (SELECT id FROM gender WHERE code = 'F'), (SELECT id FROM title WHERE code = 'MS')),
    ('Mandla', 'Van der Merwe', 'mandla.vdm@example.co.za', (SELECT id FROM gender WHERE code = 'M'), (SELECT id FROM title WHERE code = 'PROF')),
    ('Thandiwe', 'Ndlovu', 'thandiwe.ndlovu@example.co.za', (SELECT id FROM gender WHERE code = 'F'), (SELECT id FROM title WHERE code = 'MRS')),
    ('Kagiso', 'Rabade', 'kagiso.rabade@example.co.za', (SELECT id FROM gender WHERE code = 'M'), (SELECT id FROM title WHERE code = 'MR'));