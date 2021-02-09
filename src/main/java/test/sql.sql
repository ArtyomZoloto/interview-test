CREATE TABLE `WEATHER_LOG` (
  `time` text NOT NULL,
  `city` text NOT NULL,
  `temp` text NOT NULL,
  `pressure` text NOT NULL,
  `weather` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
COMMIT;

-- 1. Check and verify above DDL query.
-- 1. Show avg temp for 2 last days
-- 2. Show highest temp for every city
